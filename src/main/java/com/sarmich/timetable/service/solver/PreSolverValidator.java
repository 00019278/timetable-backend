package com.sarmich.timetable.service.solver;

import com.sarmich.timetable.model.TimeSlot;
import com.sarmich.timetable.model.response.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PreSolverValidator {

  public List<String> validate(List<OrTLesson> flattenedLessons, CompanyResponse company) {
    List<String> errors = new ArrayList<>();

    // 1. O'qituvchilar (O'zgarmaydi, har bir dars o'qituvchi vaqtini oladi)
    validateTeacherWorkload(flattenedLessons, errors);

    // 2. Sinflar (O'ZGARADI: SyncId hisobga olinadi)
    validateClassWorkload(flattenedLessons, company, errors);

    // 3. Xonalar (O'zgarmaydi)
    validateRoomCapacity(flattenedLessons, company, errors);

    return errors;
  }

  private void validateTeacherWorkload(List<OrTLesson> lessons, List<String> errors) {
    // O'qituvchilar uchun syncId muhim emas, har bir split dars baribir o'qituvchini band qiladi.
    Map<TeacherResponse, Double> teacherWorkload =
        lessons.stream()
            .filter(l -> l.teacher() != null) // Split darslarda teacher null bo'lmasligi kerak
            .collect(
                Collectors.groupingBy(
                    OrTLesson::teacher, Collectors.summingDouble(this::calculateWeeklyLoad)));

    teacherWorkload.forEach(
        (teacher, requiredWeeklyAvg) -> {
          int availableHours = calculateTotalAvailableHours(teacher.availabilities());
          if (requiredWeeklyAvg > availableHours) {
            errors.add(
                String.format(
                    "Xatolik (O'qituvchi): %s uchun haftalik %.1f soat yuklama bor, lekin bo'sh vaqti %d soat.",
                    teacher.fullName(), requiredWeeklyAvg, availableHours));
          }
        });
  }

  private void validateClassWorkload(
      List<OrTLesson> lessons, CompanyResponse company, List<String> errors) {

    int totalSchoolHours = company.daysOfWeek().size() * company.periods().size();

    // 1. Darslarni sinflar bo'yicha guruhlaymiz
    Map<Integer, List<OrTLesson>> lessonsByClass =
        lessons.stream().collect(Collectors.groupingBy(l -> l.classInfo().id()));

    // 2. Har bir sinf uchun yuklamani "aqlli" hisoblaymiz
    for (Map.Entry<Integer, List<OrTLesson>> entry : lessonsByClass.entrySet()) {
      List<OrTLesson> classLessons = entry.getValue();
      ClassResponse classInfo = classLessons.get(0).classInfo(); // Ism uchun

      // --- SYNC MANTIQI ---
      // Agar darslarda syncId bo'lsa, ularni bitta deb hisoblashimiz kerak (vaqt bo'yicha).
      // Set ishlatib, syncId larni bir marta sanaymiz.

      double totalClassLoad = 0.0;
      Set<String> processedSyncIds = new HashSet<>();

      for (OrTLesson lesson : classLessons) {
        if (lesson.syncId() != null) {
          // Agar bu syncId ni hali hisoblamagan bo'lsak, qo'shamiz
          if (!processedSyncIds.contains(lesson.syncId())) {
            totalClassLoad += calculateWeeklyLoad(lesson);
            processedSyncIds.add(lesson.syncId());
          }
          // Agar processedSyncIds da bo'lsa, demak bu parallel darsning ikkinchi bo'lagi,
          // sinf vaqti baribir ketib bo'ldi, qayta qo'shmaymiz.
        } else {
          // Oddiy dars (syncId yo'q)
          totalClassLoad += calculateWeeklyLoad(lesson);
        }
      }

      // Tekshiruvlar
      if (totalClassLoad > totalSchoolHours) {
        errors.add(
            String.format(
                "Xatolik (Sinf): '%s' sinfi uchun %.1f soat dars bor, maktab haftasi esa %d soat.",
                classInfo.name(), totalClassLoad, totalSchoolHours));
      }

      int availableHours = calculateTotalAvailableHours(classInfo.availabilities());
      if (totalClassLoad > availableHours) {
        errors.add(
            String.format(
                "Xatolik (Sinf): '%s' sinfi uchun %.1f soat dars bor, lekin sinfning ochiq vaqti %d soat.",
                classInfo.name(), totalClassLoad, availableHours));
      }
    }
  }

  private void validateRoomCapacity(
      List<OrTLesson> lessons, CompanyResponse company, List<String> errors) {

    // Xonalar uchun har bir dars (guruh bo'lsa ham) alohida xona talab qiladi.
    // Shuning uchun oddiy yig'indi to'g'ri.
    Map<Integer, Double> roomDemand = new HashMap<>();
    Map<Integer, String> roomNames = new HashMap<>(); // Xato xabari uchun ismlar

    for (OrTLesson lesson : lessons) {
      if (lesson.rooms() != null && !lesson.rooms().isEmpty()) {
        double load = calculateWeeklyLoad(lesson);
        for (RoomResponse room : lesson.rooms()) {
          roomDemand.merge(room.id(), load, Double::sum);
          roomNames.putIfAbsent(room.id(), room.name());
        }
      }
    }

    int totalSchoolHours = company.daysOfWeek().size() * company.periods().size();

    roomDemand.forEach(
        (roomId, requiredLoad) -> {
          if (requiredLoad > totalSchoolHours) {
            errors.add(
                String.format(
                    "Xatolik (Xona): '%s' xonasi uchun haftasiga %.1f soat talab bor, lekin haftada %d soat vaqt mavjud.",
                    roomNames.get(roomId), requiredLoad, totalSchoolHours));
          }
        });
  }

  private double calculateWeeklyLoad(OrTLesson lesson) {
    LessonFrequency freq = lesson.frequency() != null ? lesson.frequency() : LessonFrequency.WEEKLY;
    return (double) lesson.lessonCount() / freq.cycleLength;
  }

  private int calculateTotalAvailableHours(List<TimeSlot> availabilities) {
    if (availabilities == null || availabilities.isEmpty()) {
      return Integer.MAX_VALUE;
    }
    return availabilities.stream().mapToInt(slot -> slot.lessons().size()).sum();
  }
}
