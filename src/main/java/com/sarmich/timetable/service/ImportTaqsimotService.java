package com.sarmich.timetable.service;

import com.sarmich.timetable.domain.*;
import com.sarmich.timetable.model.request.GroupLessonDetail;
import com.sarmich.timetable.repository.*;
import com.sarmich.timetable.service.solver.LessonFrequency;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportTaqsimotService {

  private final TeacherRepository teacherRepository;
  private final SubjectRepository subjectRepository;
  private final ClassRepository classRepository;
  private final RoomRepository roomRepository;
  private final LessonRepository lessonRepository;
  private final GroupRepository groupRepository;

  /**
   * Excel fayldan (taqsimot) ma'lumotlarni o'qiydi va ma'lumotlar bazasiga import qiladi. Ushbu
   * versiya darslarni sinflar bo'yicha to'g'ri taqsimlash uchun tuzatilgan.
   */
  @Transactional
  public void importExcelToDb(MultipartFile file, Integer orgId) throws Exception {
    List<List<String>> data = readXlsx(file);
    if (data.size() < 2) {
      log.warn("Excel file is empty or contains only a header row for organization ID: {}", orgId);
      return;
    }

    List<String> headers = trimList(data.get(0));

    // Asosiy ustunlarning indekslarini dinamik ravishda topish
    int teacherIdx = 0;
    int subjectIdx = 1;
    int roomIdx = findColumnIndex(headers, Arrays.asList("xona", "room", "kabinet"));

    log.info(
        "Column indices found: Teacher={}, Subject={}, Room={}", teacherIdx, subjectIdx, roomIdx);

    // Sinf ustunlarini aniqlash: Bular sarlavhadagi o'qituvchi, fan yoki xona bo'lmagan BARCHA
    // ustunlardir.
    Map<Integer, String> classColumns = new LinkedHashMap<>();
    for (int i = 0; i < headers.size(); i++) {
      if (i != teacherIdx && i != subjectIdx && i != roomIdx) {
        classColumns.put(i, headers.get(i));
      }
    }
    if (classColumns.isEmpty()) {
      throw new IllegalStateException("Excel faylda sinflar uchun ustunlar topilmadi.");
    }
    log.info("Found {} class columns: {}", classColumns.size(), classColumns.values());

    // OPTIMALLASHTIRISH: Barcha mavjud darslarni bitta so'rov bilan oldindan o'qib olish
    Map<String, LessonEntity> existingLessonsMap =
        lessonRepository.findAllByOrgIdAndDeletedFalse(orgId).stream()
            .collect(
                Collectors.toMap(
                    lesson ->
                        String.format(
                            "%d_%d_%d_null",
                            lesson.getClassId(), lesson.getTeacherId(), lesson.getSubjectId()),
                    Function.identity(),
                    (existing, replacement) -> existing));
    log.info("Found {} existing lessons for organization {}", existingLessonsMap.size(), orgId);

    List<LessonEntity> lessonsToCreate = new ArrayList<>();
    List<LessonEntity> lessonsToUpdate = new ArrayList<>();

    // Keshlar takroriy DB so'rovlarining oldini olish uchun
    Map<String, Integer> classIdCache = new HashMap<>();
    Map<String, Integer> teacherIdCache = new HashMap<>();
    Map<String, Integer> subjectIdCache = new HashMap<>();
    Map<String, Integer> roomIdCache = new HashMap<>();

    List<List<String>> rows = data.subList(1, data.size());
    for (List<String> row : rows) {
      String teacherName = getCell(row, teacherIdx);
      String subjectName = getCell(row, subjectIdx);
      String roomName = (roomIdx != -1) ? getCell(row, roomIdx) : null;

      if (teacherName == null || subjectName == null) continue;

      Integer teacherId = teacherIdCache.computeIfAbsent(teacherName, n -> ensureTeacher(orgId, n));
      Integer subjectId = subjectIdCache.computeIfAbsent(subjectName, n -> ensureSubject(orgId, n));
      Integer roomId =
          (roomName != null)
              ? roomIdCache.computeIfAbsent(roomName, n -> ensureRoom(orgId, n))
              : null;

      // Har bir sinf ustuni bo'yicha aylanib chiqamiz
      for (Map.Entry<Integer, String> classEntry : classColumns.entrySet()) {
        Integer classColIndex = classEntry.getKey();
        String className = classEntry.getValue();

        String rawHours = getCell(row, classColIndex);
        Double hoursDouble = parseHoursDouble(rawHours);
        if (hoursDouble == null) continue;

        Integer classId = classIdCache.computeIfAbsent(className, n -> ensureClass(orgId, n));

        String lessonKey = String.format("%d_%d_%d_null", classId, teacherId, subjectId);
        LessonEntity existingLesson = existingLessonsMap.get(lessonKey);

        if (existingLesson != null) {
          if (hoursDouble == 0.5) {
            existingLesson.setLessonCount(1);
            existingLesson.setFrequency(LessonFrequency.BI_WEEKLY);
          } else {
            existingLesson.setLessonCount(hoursDouble.intValue());
            existingLesson.setFrequency(LessonFrequency.WEEKLY);
          }

          if (roomId != null) {
            List<Integer> rooms =
                existingLesson.getRoomIds() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(existingLesson.getRoomIds());
            if (!rooms.contains(roomId)) {
              rooms.add(roomId);
              existingLesson.setRoomIds(rooms);
            }
          }
          lessonsToUpdate.add(existingLesson);
        } else {
          LessonEntity newLesson = new LessonEntity();
          newLesson.setOrgId(orgId);
          newLesson.setClassId(classId);
          newLesson.setTeacherId(teacherId);
          newLesson.setSubjectId(subjectId);
          if (hoursDouble == 0.5) {
            newLesson.setLessonCount(1);
            newLesson.setFrequency(LessonFrequency.BI_WEEKLY);
          } else {
            newLesson.setLessonCount(hoursDouble.intValue());
            newLesson.setFrequency(LessonFrequency.WEEKLY);
          }
          if (roomId != null) {
            newLesson.setRoomIds(Collections.singletonList(roomId));
          }
          lessonsToCreate.add(newLesson);
          existingLessonsMap.put(lessonKey, newLesson);
        }
      }
    }

    // Guruhlash logikasi
    Map<Integer, Map<Integer, List<LessonEntity>>> classSubjectLessons = new HashMap<>();
    List<LessonEntity> allLessons = new ArrayList<>();
    allLessons.addAll(lessonsToCreate);
    allLessons.addAll(lessonsToUpdate);

    for (LessonEntity lesson : allLessons) {
      classSubjectLessons
          .computeIfAbsent(lesson.getClassId(), k -> new HashMap<>())
          .computeIfAbsent(lesson.getSubjectId(), k -> new ArrayList<>())
          .add(lesson);
    }

    List<LessonEntity> finalLessonsToSave = new ArrayList<>();

    for (Map<Integer, List<LessonEntity>> subjectMap : classSubjectLessons.values()) {
      for (List<LessonEntity> lessons : subjectMap.values()) {
        if (lessons.size() > 1) {
          // Guruhlash kerak
          LessonEntity mainLesson = lessons.get(0);
          List<GroupLessonDetail> groupDetails = new ArrayList<>();

          for (int i = 0; i < lessons.size(); i++) {
            LessonEntity l = lessons.get(i);
            GroupEntity group = ensureGroup(orgId, l.getClassId(), "Group " + (i + 1));
            groupDetails.add(
                new GroupLessonDetail(
                    group.getId(), l.getTeacherId(), l.getSubjectId(), l.getRoomIds()));

            if (i > 0) {
              if (l.getId() != null) {
                l.setDeleted(true);
                finalLessonsToSave.add(l);
              }
            }
          }
          mainLesson.setGroupDetails(groupDetails);
          finalLessonsToSave.add(mainLesson);
        } else {
          finalLessonsToSave.add(lessons.get(0));
        }
      }
    }

    // Barcha o'zgarishlarni bittada (batch) saqlash
    if (!finalLessonsToSave.isEmpty()) {
      lessonRepository.saveAll(finalLessonsToSave);
      log.info(
          "Successfully saved {} lessons for organization {}.", finalLessonsToSave.size(), orgId);
    }
  }

  // --- "Ensure" metodlari: Mavjudlikni tekshirish va kerak bo'lsa yaratish ---

  private Integer ensureTeacher(Integer orgId, String fullName) {
    return teacherRepository
        .findFirstByOrgIdAndDeletedFalseAndFullNameIgnoreCase(orgId, fullName)
        .map(TeacherEntity::getId)
        .orElseGet(
            () -> {
              TeacherEntity t = new TeacherEntity();
              t.setOrgId(orgId);
              t.setFullName(fullName);
              t.setShortName(fullName);
              return teacherRepository.save(t).getId();
            });
  }

  private Integer ensureSubject(Integer orgId, String name) {
    return subjectRepository
        .findFirstByOrgIdAndDeletedFalseAndNameIgnoreCase(orgId, name)
        .map(SubjectEntity::getId)
        .orElseGet(
            () -> {
              SubjectEntity s = new SubjectEntity();
              s.setOrgId(orgId);
              s.setName(name);
              s.setShortName(name);
              return subjectRepository.save(s).getId();
            });
  }

  private Integer ensureClass(Integer orgId, String name) {
    return classRepository
        .findFirstByOrgIdAndDeletedFalseAndNameIgnoreCase(orgId, name)
        .map(ClassEntity::getId)
        .orElseGet(
            () -> {
              ClassEntity c = new ClassEntity();
              c.setOrgId(orgId);
              c.setName(name);
              c.setShortName(name);
              return classRepository.save(c).getId();
            });
  }

  private Integer ensureRoom(Integer orgId, String name) {
    return roomRepository
        .findFirstByOrgIdAndNameIgnoreCase(orgId, name)
        .map(RoomEntity::getId)
        .orElseGet(
            () -> {
              RoomEntity r = new RoomEntity();
              r.setOrgId(orgId);
              r.setName(name);
              r.setShortName(name);
              return roomRepository.save(r).getId();
            });
  }

  private GroupEntity ensureGroup(Integer orgId, Integer classId, String name) {
    return groupRepository.findAllByClassIdAndOrgIdAndDeletedFalse(classId, orgId).stream()
        .filter(g -> g.getName().equalsIgnoreCase(name))
        .findFirst()
        .orElseGet(
            () -> {
              GroupEntity g = new GroupEntity();
              g.setOrgId(orgId);
              g.setClassId(classId);
              g.setName(name);
              return groupRepository.save(g);
            });
  }

  // --- Excel faylini o'qish uchun yordamchi metodlar ---

  private static List<List<String>> readXlsx(MultipartFile file) throws Exception {
    try (InputStream is = file.getInputStream();
        Workbook wb = new XSSFWorkbook(is)) {
      Sheet sheet = wb.getSheetAt(0);
      List<List<String>> data = new ArrayList<>();
      int maxCols = 0;
      Row headerRow = sheet.getRow(0);
      if (headerRow != null) {
        maxCols = headerRow.getLastCellNum();
      }

      for (Row row : sheet) {
        if (isRowEmpty(row, maxCols)) continue;

        List<String> vals = new ArrayList<>();
        for (int c = 0; c < maxCols; c++) {
          Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
          vals.add(cellToString(cell));
        }
        data.add(vals);
      }
      return data;
    }
  }

  private static String cellToString(Cell cell) {
    if (cell == null) return "";
    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue().trim();
      case NUMERIC -> {
        double d = cell.getNumericCellValue();
        if (Math.floor(d) == d) yield String.valueOf((long) d);
        yield String.valueOf(d);
      }
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      case FORMULA -> {
        try {
          yield cell.getStringCellValue().trim();
        } catch (Exception e) {
          try {
            double d = cell.getNumericCellValue();
            if (Math.floor(d) == d) yield String.valueOf((long) d);
            yield String.valueOf(d);
          } catch (Exception ex) {
            yield "";
          }
        }
      }
      default -> "";
    };
  }

  private static boolean isRowEmpty(Row row, int maxCols) {
    if (row == null) return true;
    for (int c = 0; c < maxCols; c++) {
      Cell cell = row.getCell(c);
      if (cell != null && cell.getCellType() != CellType.BLANK) {
        String value = cellToString(cell);
        if (value != null && !value.trim().isEmpty()) {
          return false;
        }
      }
    }
    return true;
  }

  private static List<String> trimList(List<String> list) {
    return list.stream().map(s -> s == null ? "" : s.trim()).collect(Collectors.toList());
  }

  private static String getCell(List<String> row, int idx) {
    if (idx < 0 || idx >= row.size()) return null;
    String s = row.get(idx);
    if (s == null) return null;
    s = s.trim();
    return s.isEmpty() ? null : s;
  }

  private static int findColumnIndex(List<String> headers, List<String> candidates) {
    for (int i = 0; i < headers.size(); i++) {
      String header = headers.get(i).toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
      for (String candidate : candidates) {
        if (header.contains(candidate.toLowerCase(Locale.ROOT).replaceAll("\\s+", ""))) {
          return i;
        }
      }
    }
    return -1;
  }

  private static Double parseHoursDouble(String raw) {
    if (raw == null) return null;
    String s = raw.trim().replace(',', '.');
    if (s.isEmpty()) return null;
    try {
      double d = Double.parseDouble(s);
      if (d <= 0) return null;
      return d;
    } catch (NumberFormatException e) {
      log.trace("Could not parse '{}' as a number, skipping.", raw);
      return null;
    }
  }
}
