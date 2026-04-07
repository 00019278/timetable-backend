package com.sarmich.timetable.service.solver;

import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import com.sarmich.timetable.model.response.OrTLesson;
import java.util.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class SoftConstraintProvider {

  /** Soft constraintlarni qo'llash. Maqsad: Oynalarni (gaps) minimallashtirish. */
  public void apply(
      CpModel model,
      ModelVariables vars,
      ModelData data,
      LinearExprBuilder objective,
      ApplySoftConstraint options) {

    if (!Boolean.TRUE.equals(options.getApplySoftConstraint())) {
      log.info("Soft constraints are disabled.");
      return;
    }

    log.info("Applying soft constraints for gap minimization...");

    int hpd = data.getHoursPerDay();
    int days = data.getDays();

    // O'qituvchilar uchun gap minimizatsiya
    if (Boolean.TRUE.equals(options.getApplyContinuityPenaltyTeacher())) {
      applyGapPenaltyForResource(
          model,
          vars,
          data,
          objective,
          ResourceType.TEACHER,
          options.getApplyContinuityPenaltyTeacherPenalty(),
          hpd,
          days);
    }

    // Sinflar uchun gap minimizatsiya
    if (Boolean.TRUE.equals(options.getApplyContinuityPenaltyClass())) {
      applyGapPenaltyForResource(
          model,
          vars,
          data,
          objective,
          ResourceType.CLASS,
          options.getApplyContinuityPenaltyClassPenalty(),
          hpd,
          days);
    }

    // Fanlarni kunlik taqsimlash (bir kunda bitta fandan ko'p bo'lmasligi)
    if (Boolean.TRUE.equals(options.getApplyDailySubjectDistribution())) {
      applyDailySubjectDistribution(
          model,
          vars,
          data,
          objective,
          options.getApplyDailySubjectDistributionPenalty(),
          hpd,
          days);
    }
  }

  /**
   * Berilgan resurs turi (o'qituvchi yoki sinf) uchun gap penaltyni qo'llash. Gap = bo'sh soat,
   * oldin ham keyin ham dars bor.
   */
  private void applyGapPenaltyForResource(
      CpModel model,
      ModelVariables vars,
      ModelData data,
      LinearExprBuilder objective,
      ResourceType type,
      int penalty,
      int hpd,
      int days) {

    // Darslarni resurs bo'yicha guruhlash
    Map<Integer, List<OrTLesson>> lessonsByResource = new HashMap<>();

    for (OrTLesson lesson : data.getLessons()) {
      Integer resourceId =
          switch (type) {
            case TEACHER ->
                lesson.teacher() != null
                    ? data.getTeacherIdToIndex().get(lesson.teacher().id())
                    : null;
            case CLASS ->
                lesson.classInfo() != null
                    ? data.getClassIdToIndex().get(lesson.classInfo().id())
                    : null;
          };

      if (resourceId != null) {
        lessonsByResource.computeIfAbsent(resourceId, k -> new ArrayList<>()).add(lesson);
      }
    }

    // Har bir resurs uchun
    for (Map.Entry<Integer, List<OrTLesson>> entry : lessonsByResource.entrySet()) {
      Integer resourceIdx = entry.getKey();
      List<OrTLesson> lessons = entry.getValue();

      if (lessons.size() < 2) continue; // Gap faqat 2+ dars bo'lganda bo'lishi mumkin

      // Har bir kun uchun
      for (int day = 0; day < days; day++) {
        final int currentDay = day;
        int dayStart = day * hpd;

        // Bu kun uchun busy[h] BoolVar yaratish
        // busy[h] = darslardan kamida bittasi h soatda bo'lsa true
        BoolVar[] busyHour = new BoolVar[hpd];

        for (int h = 0; h < hpd; h++) {
          final int hour = h;
          List<BoolVar> lessonAtHour = new ArrayList<>();

          for (OrTLesson lesson : lessons) {
            IntVar startVar = vars.getLessonStartVars().get(lesson.id());
            if (startVar == null) continue;

            int duration = lesson.period() != null ? lesson.period() : 1;

            // Bu dars bu soatda bo'lishi mumkinmi?
            // Dars [start, start+duration) oralig'ida bo'ladi.
            // Soat h da dars bor demak: start <= dayStart+h < start+duration
            // Ya'ni: start <= dayStart+h AND dayStart+h < start+duration
            // Bu: start <= dayStart+h AND start > dayStart+h-duration
            // Simplest: dayStart+h - duration + 1 <= start <= dayStart+h

            int targetSlot = dayStart + hour;

            // lessonCovers = (start <= targetSlot && start > targetSlot - duration)
            // = (start <= targetSlot) AND (start >= targetSlot - duration + 1)
            for (int offset = 0; offset < duration; offset++) {
              int requiredStart = targetSlot - offset;
              if (requiredStart >= 0 && requiredStart < data.getHoursCount()) {
                BoolVar exactStart =
                    model.newBoolVar(
                        type.name().toLowerCase()
                            + "_"
                            + resourceIdx
                            + "_l"
                            + lesson.id()
                            + "_at"
                            + requiredStart);
                model.addEquality(startVar, requiredStart).onlyEnforceIf(exactStart);
                model.addDifferent(startVar, requiredStart).onlyEnforceIf(exactStart.not());
                lessonAtHour.add(exactStart);
              }
            }
          }

          // busyHour[h] = OR(lessonAtHour)
          if (!lessonAtHour.isEmpty()) {
            busyHour[h] =
                model.newBoolVar(
                    type.name().toLowerCase()
                        + "_"
                        + resourceIdx
                        + "_d"
                        + currentDay
                        + "_busy"
                        + hour);
            model.addMaxEquality(busyHour[h], lessonAtHour.toArray(new BoolVar[0]));
          } else {
            // Bu soatda hech qanday dars mumkin emas
            busyHour[h] =
                model.newBoolVar(
                    type.name().toLowerCase()
                        + "_"
                        + resourceIdx
                        + "_d"
                        + currentDay
                        + "_busy"
                        + hour);
            model.addEquality(busyHour[h], 0);
          }
        }

        // Gap[h] = NOT busy[h] AND (exists busy[i] where i < h) AND (exists busy[j]
        // where j > h)
        for (int h = 1; h < hpd - 1; h++) {
          // anyBefore[h] = OR(busy[0]...busy[h-1])
          BoolVar[] before = new BoolVar[h];
          System.arraycopy(busyHour, 0, before, 0, h);
          BoolVar anyBefore =
              model.newBoolVar(
                  type.name().toLowerCase()
                      + "_"
                      + resourceIdx
                      + "_d"
                      + currentDay
                      + "_anyBefore"
                      + h);
          model.addMaxEquality(anyBefore, before);

          // anyAfter[h] = OR(busy[h+1]...busy[hpd-1])
          int afterCount = hpd - h - 1;
          BoolVar[] after = new BoolVar[afterCount];
          System.arraycopy(busyHour, h + 1, after, 0, afterCount);
          BoolVar anyAfter =
              model.newBoolVar(
                  type.name().toLowerCase()
                      + "_"
                      + resourceIdx
                      + "_d"
                      + currentDay
                      + "_anyAfter"
                      + h);
          model.addMaxEquality(anyAfter, after);

          // gap[h] = NOT busy[h] AND anyBefore AND anyAfter
          BoolVar gap =
              model.newBoolVar(
                  type.name().toLowerCase() + "_" + resourceIdx + "_d" + currentDay + "_gap" + h);
          // gap = (1 - busy[h]) * anyBefore * anyAfter
          // This is: gap => !busy[h], gap => anyBefore, gap => anyAfter
          // And: !busy[h] AND anyBefore AND anyAfter => gap

          model.addEquality(busyHour[h], 0).onlyEnforceIf(gap);
          model.addEquality(anyBefore, 1).onlyEnforceIf(gap);
          model.addEquality(anyAfter, 1).onlyEnforceIf(gap);

          // If all conditions are satisfied, gap should be true
          // (1-busy[h]) + anyBefore + anyAfter >= 3 => gap
          // But simpler: we add penalty for gap being 1
          // gap can be 1 only if conditions are met

          // Add penalty to objective
          objective.addTerm(gap, penalty);
        }
      }
    }

    log.debug("Applied gap penalty for {} resources with penalty {}", type, penalty);
  }

  /** Bir kunda bitta fandan ko'p dars bo'lmasligi uchun soft constraint. */
  private void applyDailySubjectDistribution(
      CpModel model,
      ModelVariables vars,
      ModelData data,
      LinearExprBuilder objective,
      int penalty,
      int hpd,
      int days) {

    // Darslarni (sinf, fan) juftligi bo'yicha guruhlash
    Map<String, List<OrTLesson>> lessonsByClassSubject = new HashMap<>();

    for (OrTLesson lesson : data.getLessons()) {
      if (lesson.classInfo() == null || lesson.subject() == null) continue;
      String key = lesson.classInfo().id() + "_" + lesson.subject().id();
      lessonsByClassSubject.computeIfAbsent(key, k -> new ArrayList<>()).add(lesson);
    }

    // Har bir (sinf, fan) juftligi uchun
    for (List<OrTLesson> lessons : lessonsByClassSubject.values()) {
      if (lessons.size() < 2) continue;

      // Har bir kun uchun, 2 dan ortiq bo'lsa penalty
      for (int day = 0; day < days; day++) {
        final int currentDay = day;
        int dayStart = day * hpd;

        List<BoolVar> lessonsOnDay = new ArrayList<>();

        for (OrTLesson lesson : lessons) {
          IntVar startVar = vars.getLessonStartVars().get(lesson.id());
          if (startVar == null) continue;

          // Bu dars bu kunda bo'lishini tekshirish
          BoolVar onThisDay = model.newBoolVar("subject_dist_" + lesson.id() + "_d" + currentDay);

          // onThisDay = (start >= dayStart && start < dayStart + hpd)
          // Simpler: start / hpd == day
          // We need: start in [dayStart, dayStart + hpd - 1]
          model.addGreaterOrEqual(startVar, dayStart).onlyEnforceIf(onThisDay);
          model.addLessThan(startVar, dayStart + hpd).onlyEnforceIf(onThisDay);

          lessonsOnDay.add(onThisDay);
        }

        if (lessonsOnDay.size() >= 2) {
          // Sum > 1 bo'lsa penalty qo'shish
          // excessVar = max(0, sum - 1)
          IntVar sumVar = model.newIntVar(0, lessonsOnDay.size(), "subj_sum_d" + currentDay);
          model.addEquality(sumVar, LinearExpr.sum(lessonsOnDay.toArray(new BoolVar[0])));

          IntVar excessVar =
              model.newIntVar(0, lessonsOnDay.size() - 1, "subj_excess_d" + currentDay);
          // excess = max(0, sum - 1)
          IntVar sumMinus1 =
              model.newIntVar(-1, lessonsOnDay.size() - 1, "subj_sum_minus1_d" + currentDay);
          model.addEquality(LinearExpr.newBuilder().add(sumVar).add(-1).build(), sumMinus1);
          model.addMaxEquality(excessVar, new IntVar[] {model.newConstant(0), sumMinus1});

          objective.addTerm(excessVar, penalty);
        }
      }
    }

    log.debug("Applied daily subject distribution penalty with penalty {}", penalty);
  }

  private enum ResourceType {
    TEACHER,
    CLASS
  }
}
