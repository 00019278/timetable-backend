package com.sarmich.timetable.service.solver;

import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.IntVar;
import com.sarmich.timetable.domain.enums.RoomType;
import com.sarmich.timetable.model.TimeSlot;
import com.sarmich.timetable.model.response.ClassResponse;
import com.sarmich.timetable.model.response.OrTLesson;
import com.sarmich.timetable.model.response.RoomResponse;
import com.sarmich.timetable.model.response.TeacherResponse;
import com.sarmich.timetable.utils.Util;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class VariableFactory {

  public ModelVariables createVariables(CpModel model, ModelData data) {
    ModelVariables variables = new ModelVariables();
    createVariablesWithBlockSupport(model, variables, data);

    log.info(
        "Created {} assignment variables (boolean) for the model.",
        variables.getAssignmentVars().size());
    return variables;
  }

  private void createVariablesWithBlockSupport(
      CpModel model, ModelVariables variables, ModelData data) {

    for (OrTLesson lesson : data.getLessons()) {
      // 1. Period (Dars davomiyligi) tekshiruvi
      final int period = Util.getNotNull(lesson.period(), 1);
      if (period < 1) continue;

      // 2. Chastotani aniqlash (Weekly, Bi-weekly, etc.)
      LessonFrequency freq = Util.getNotNull(lesson.frequency(), LessonFrequency.WEEKLY);

      // 3. Indekslarni olish
      Integer cIdx = data.getClassIdToIndex().get(lesson.classInfo().id());
      Integer tIdx = data.getTeacherIdToIndex().get(lesson.teacher().id());
      Integer sIdx = data.getSubjectIdToIndex().get(lesson.subject().id());

      // Agar resurslar (o'qituvchi, sinf, fan) o'chirilgan bo'lsa, tashlab ketamiz
      if (cIdx == null || tIdx == null || sIdx == null) {
        log.warn("Skipping lesson ID {} due to missing resource index.", lesson.id());
        continue;
      }

      ClassResponse classObj = data.getClassIndexToObj().get(cIdx);
      TeacherResponse teacherObj = data.getTeacherIndexToObj().get(tIdx);

      // --- YANGI QISM: HAFTA O'ZGARUVCHISI (Shared Week Variable) ---
      IntVar sharedWeekVar = null;

      if (freq != LessonFrequency.WEEKLY) {
        // A) Agar darsda SYNC_ID bo'lsa (Parallel guruhlar), umumiy mapdan qidiramiz
        if (lesson.syncId() != null
            && variables.getSyncIdToWeekVar().containsKey(lesson.syncId())) {
          sharedWeekVar = variables.getSyncIdToWeekVar().get(lesson.syncId());
        }
        // B) Agar dars ID bo'yicha oldin yaratilgan bo'lsa (bitta darsning turli slotlari uchun)
        else if (variables.getLessonIdToWeekVar().containsKey(lesson.id())) {
          sharedWeekVar = variables.getLessonIdToWeekVar().get(lesson.id());
        }
        // C) Yaratilmagan bo'lsa, yangisini yaratamiz
        else {
          String weekVarName = String.format("week_l%d", lesson.id());
          // Masalan, Bi-weekly uchun 0..1 oralig'ida
          sharedWeekVar = model.newIntVar(0, freq.cycleLength - 1, weekVarName);
        }

        // Yaratilgan yoki olingan o'zgaruvchini KESHLARGA saqlaymiz
        variables.getLessonIdToWeekVar().put(lesson.id(), sharedWeekVar);
        if (lesson.syncId() != null) {
          variables.getSyncIdToWeekVar().put(lesson.syncId(), sharedWeekVar);
        }
      }

      // 4. Barcha mumkin bo'lgan soatlar (slots) bo'yicha aylanish
      for (int h_start = 0; h_start < data.getHoursCount(); h_start++) {

        // A. Blok sig'ishini tekshirish (Masalan, kun oxiriga 2 soatlik dars sig'adimi?)
        if (!isValidStartingSlot(h_start, period, data.getHoursPerDay())) {
          continue;
        }

        // B. O'qituvchi va Sinfning umumiy bo'sh vaqtini tekshirish
        if (!areTeacherAndClassAvailableForBlock(
            classObj, teacherObj, h_start, period, data.getHoursPerDay())) {
          continue;
        }

        // C. Xonalarni tekshirish va O'zgaruvchi (BoolVar) yaratish
        if (data.isUseRooms()) {
          for (RoomResponse room : data.getAllRooms()) {
            // Xona darsga mosmi (laboratoriya, oddiy) va shu vaqtda ochiqmi?
            if (isLessonPlacementValid(lesson, room)
                && isRoomAvailableForBlock(room, h_start, period, data.getHoursPerDay())) {

              createBoolVarForBlock(
                  model,
                  variables,
                  cIdx,
                  tIdx,
                  sIdx,
                  h_start,
                  room.id(),
                  period,
                  sharedWeekVar,
                  lesson.id());
            }
          }
        } else {
          // Xonasiz rejim (r=0)
          createBoolVarForBlock(
              model, variables, cIdx, tIdx, sIdx, h_start, 0, period, sharedWeekVar, lesson.id());
        }
      }
    }
  }

  // --- O'zgaruvchi yaratish va Keshlash ---

  private void createBoolVarForBlock(
      CpModel model,
      ModelVariables vars,
      int c,
      int t,
      int s,
      int h_start,
      int r,
      int period,
      IntVar weekVar,
      int lessonId) {

    // Unique Key generatsiyasi.
    // Format: c{class}_t{teacher}_s{subject}_h{hour}_r{room}_p{period}_l{lessonId}
    String varKey =
        String.format("c%d_t%d_s%d_h%d_r%d_p%d_l%d", c, t, s, h_start, r, period, lessonId);

    // Asosiy BoolVar ("Shu variant tanlandimi?")
    BoolVar blockAssignment = model.newBoolVar(varKey);
    vars.getAssignmentVars().put(varKey, blockAssignment);

    // 1. Hafta o'zgaruvchisini bog'lash (Bi-weekly uchun)
    if (weekVar != null) {
      vars.getLessonWeekVars().put(varKey, weekVar);
    }

    // 2. YANGI: Ketma-ketlik (Sequential) uchun dars boshlanish vaqtini saqlash
    // Key: "105_2" -> Dars ID 105, 2-soatda boshlanyapti
    String startKey = lessonId + "_" + h_start;
    vars.getLessonStartVars()
        .computeIfAbsent(startKey, k -> new ArrayList<>())
        .add(blockAssignment);

    // 3. Tezkor qidiruv uchun keshlarni to'ldirish
    for (int k = 0; k < period; k++) {
      int currentHour = h_start + k;

      // O'qituvchi jadvali uchun
      vars.getLessonsByTeacherHour()
          .computeIfAbsent(t + "_" + currentHour, key -> new ArrayList<>())
          .add(blockAssignment);

      // Sinf jadvali uchun
      vars.getLessonsByClassHour()
          .computeIfAbsent(c + "_" + currentHour, key -> new ArrayList<>())
          .add(blockAssignment);

      // Xona jadvali uchun (faqat haqiqiy xona bo'lsa)
      if (r > 0 || vars.getLessonsByRoomHour().containsKey(r + "_" + currentHour)) {
        vars.getLessonsByRoomHour()
            .computeIfAbsent(r + "_" + currentHour, key -> new ArrayList<>())
            .add(blockAssignment);
      }
    }

    // Darslar sonini sanash (Count Constraints) uchun guruhlash
    vars.getLessonsByClassTeacherSubject()
        .computeIfAbsent(c + "_" + t + "_" + s, key -> new ArrayList<>())
        .add(blockAssignment);
  }

  // --- Yordamchi Metodlar (Validatsiya) ---

  private boolean isValidStartingSlot(int startHour, int period, int hoursPerDay) {
    if (period == 1) return true;
    // Dars kun chegarasidan o'tib ketmasligi kerak (Masalan, Juma tugab, Shanbaga o'tmasin)
    return (startHour % hoursPerDay) + period <= hoursPerDay;
  }

  private boolean areTeacherAndClassAvailableForBlock(
      ClassResponse classObj,
      TeacherResponse teacherObj,
      int startHour,
      int period,
      int hoursPerDay) {
    // Blokning barcha soatlarida (period davomida) o'qituvchi va sinf bo'sh bo'lishi kerak
    for (int k = 0; k < period; k++) {
      if (!isAvailable(classObj, teacherObj, startHour + k, hoursPerDay)) {
        return false;
      }
    }
    return true;
  }

  private boolean isRoomAvailableForBlock(
      RoomResponse room, int startHour, int period, int hoursPerDay) {
    for (int k = 0; k < period; k++) {
      if (!isAvailableAtHour(room.availabilities(), startHour + k, hoursPerDay)) {
        return false;
      }
    }
    return true;
  }

  private boolean isLessonPlacementValid(OrTLesson lesson, RoomResponse room) {
    List<Integer> requiredRoomIds =
        Util.getNotNull(lesson.rooms(), Collections.<RoomResponse>emptyList()).stream()
            .map(RoomResponse::id)
            .toList();

    if (!requiredRoomIds.isEmpty()) {
      return requiredRoomIds.contains(room.id());
    } else {
      return room.type() == RoomType.SHARED;
    }
  }

  private boolean isAvailable(
      ClassResponse classObj, TeacherResponse teacherObj, int hourIndex, int hoursPerDay) {
    return isAvailableAtHour(classObj.availabilities(), hourIndex, hoursPerDay)
        && isAvailableAtHour(teacherObj.availabilities(), hourIndex, hoursPerDay);
  }

  private boolean isAvailableAtHour(List<TimeSlot> availabilities, int hourIndex, int hoursPerDay) {
    // Agar availability bo'sh bo'lsa, demak har doim bo'sh
    if (availabilities == null || availabilities.isEmpty()) return true;

    // Global soat (0..40) dan Hafta kuni va Kun soatini ajratib olamiz
    DayOfWeek currentDay = DayOfWeek.of((hourIndex / hoursPerDay) + 1);
    int currentHourOfDay = (hourIndex % hoursPerDay) + 1;

    // Ro'yxatdan qidiramiz
    return availabilities.stream()
        .anyMatch(
            slot -> slot.dayOfWeek() == currentDay && slot.lessons().contains(currentHourOfDay));
  }
}
