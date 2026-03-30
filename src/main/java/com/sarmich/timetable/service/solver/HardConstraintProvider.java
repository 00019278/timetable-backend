package com.sarmich.timetable.service.solver;

import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.Literal;
import com.sarmich.timetable.model.response.OrTLesson;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class HardConstraintProvider {

  public void apply(CpModel model, ModelVariables vars, ModelData data) {
    log.info("Applying all hard constraints to the model...");

    // 1. Resurslar ziddiyati (O'qituvchi, Sinf, Xona)
    addResourceExclusivityConstraint(model, vars, data);

    // 2. Darslar soni va kunlik limit
    addWeeklyAndDailySubjectConstraints(model, vars, data);

    // 3. Xonalar simmetriyasi
    addRoomSymmetryBreaking(model, vars, data);

    // 4. YANGI: Parallel (Sinxron) darslar (SyncId)
    addSimultaneousLessonsConstraint(model, vars, data);
  }

  // ... addResourceExclusivityConstraint (ESKI KOD O'ZGARISHSIZ QOLADI) ...
  private void addResourceExclusivityConstraint(
      CpModel model, ModelVariables vars, ModelData data) {
    // ... (Eski kodingizdagi kabi, faqat applySmartLayeredConflict chaqiriladi)
    for (List<BoolVar> lessons : vars.getLessonsByTeacherHour().values()) {
      applySmartLayeredConflict(model, vars, lessons);
    }
    for (List<BoolVar> lessons : vars.getLessonsByClassHour().values()) {
      applySmartLayeredConflict(model, vars, lessons);
    }
    if (data.isUseRooms()) {
      for (List<BoolVar> lessons : vars.getLessonsByRoomHour().values()) {
        applySmartLayeredConflict(model, vars, lessons);
      }
    }
  }

  // ... applySmartLayeredConflict (ESKI KOD O'ZGARISHSIZ) ...
  private void applySmartLayeredConflict(
      CpModel model, ModelVariables vars, List<BoolVar> lessonsInSlot) {
    // ... (Eski kodingizdagi kabi haftalarni tekshirish)
    if (lessonsInSlot.size() <= 1) return;
    for (int i = 0; i < lessonsInSlot.size(); i++) {
      for (int j = i + 1; j < lessonsInSlot.size(); j++) {
        BoolVar var1 = lessonsInSlot.get(i);
        BoolVar var2 = lessonsInSlot.get(j);
        IntVar week1 = vars.getLessonWeekVars().get(var1.getName());
        IntVar week2 = vars.getLessonWeekVars().get(var2.getName());

        if (week1 == null && week2 == null) {
          model.addImplication(var1, var2.not());
        } else if (week1 == null || week2 == null) {
          model.addImplication(var1, var2.not());
        } else {
          model.addDifferent(week1, week2).onlyEnforceIf(new Literal[] {var1, var2});
        }
      }
    }
  }

  // ... addWeeklyAndDailySubjectConstraints (O'ZGARTIRILDI: OrTLesson ishlatiladi) ...
  private void addWeeklyAndDailySubjectConstraints(
      CpModel model, ModelVariables vars, ModelData data) {
    log.debug("Applying weekly and daily subject constraints.");

    for (OrTLesson lesson : data.getLessons()) {
      Integer cIdx = data.getClassIdToIndex().get(lesson.classInfo().id());
      Integer tIdx = data.getTeacherIdToIndex().get(lesson.teacher().id());
      Integer sIdx = data.getSubjectIdToIndex().get(lesson.subject().id());
      int totalRequiredBlocks = lesson.lessonCount();

      if (cIdx == null || tIdx == null || sIdx == null || totalRequiredBlocks == 0) continue;

      List<BoolVar> allPossibleBlocks =
          vars.getLessonsByClassTeacherSubject().get(cIdx + "_" + tIdx + "_" + sIdx);

      if (allPossibleBlocks == null || allPossibleBlocks.isEmpty()) continue;

      // 1. Total Count (LessOrEqual)
      model.addLessOrEqual(
          LinearExpr.sum(allPossibleBlocks.toArray(new BoolVar[0])), totalRequiredBlocks);

      // 2. Daily Limit
      for (int day = 0; day < data.getDays(); day++) {
        List<BoolVar> dailyBlocks =
            filterBlocksForDay(allPossibleBlocks, day, data.getHoursPerDay());
        if (!dailyBlocks.isEmpty()) {
          int maxDailyBlocks = (totalRequiredBlocks >= 4) ? 2 : 1;
          model.addLessOrEqual(LinearExpr.sum(dailyBlocks.toArray(new BoolVar[0])), maxDailyBlocks);
        }
      }
    }
  }

  // ... filterBlocksForDay (O'ZGARISHSIZ) ...
  private List<BoolVar> filterBlocksForDay(List<BoolVar> allBlocks, int dayIndex, int hoursPerDay) {
    List<BoolVar> dailyBlocks = new ArrayList<>();
    int startHourOfDay = dayIndex * hoursPerDay;
    int endHourOfDay = startHourOfDay + hoursPerDay;
    for (BoolVar var : allBlocks) {
      int h_start = Integer.parseInt(var.getName().split("_")[3].substring(1));
      if (h_start >= startHourOfDay && h_start < endHourOfDay) {
        dailyBlocks.add(var);
      }
    }
    return dailyBlocks;
  }

  // ... addRoomSymmetryBreaking (O'ZGARISHSIZ) ...
  private void addRoomSymmetryBreaking(CpModel model, ModelVariables vars, ModelData data) {
    // ... (Eski kodingizdagi kabi)
    if (!data.isUseRooms()) return;
    // ...
  }

  // =================================================================================
  // 4. YANGI: PARALLEL (SINXRON) DARSLAR CHEKLOVI
  // =================================================================================
  private void addSimultaneousLessonsConstraint(
      CpModel model, ModelVariables vars, ModelData data) {
    log.debug("Applying simultaneous (sync) lessons constraints.");

    // 1. Darslarni syncId bo'yicha guruhlaymiz
    Map<String, List<OrTLesson>> syncGroups =
        data.getLessons().stream()
            .filter(l -> l.syncId() != null)
            .collect(Collectors.groupingBy(OrTLesson::syncId));

    for (List<OrTLesson> group : syncGroups.values()) {
      if (group.size() < 2) continue; // Juftlik bo'lishi kerak

      // Guruhdagi birinchi dars "Leader"
      OrTLesson leader = group.get(0);

      // Qolgan darslarni "Follower" qilib Leaderga bog'laymiz
      for (int i = 1; i < group.size(); i++) {
        OrTLesson follower = group.get(i);

        // Barcha vaqt slotlari bo'yicha aylanamiz
        for (int h = 0; h < data.getHoursCount(); h++) {

          // Leader va Follower ning shu soatda boshlanadigan variantlarini topamiz
          // VariableFactory da "lessonStartVars" mapini to'ldirgan edik (LessonId + Hour)
          String leaderKey = leader.id() + "_" + h;
          String followerKey = follower.id() + "_" + h;

          List<BoolVar> leaderVars = vars.getLessonStartVars().get(leaderKey);
          List<BoolVar> followerVars = vars.getLessonStartVars().get(followerKey);

          if (leaderVars != null
              && !leaderVars.isEmpty()
              && followerVars != null
              && !followerVars.isEmpty()) {

            // Agar xonalar tufayli bir nechta variant bo'lsa, ularning yig'indisini olamiz
            // (Ya'ni: "Leader shu soatda bormi?" == "Follower shu soatda bormi?")
            LinearExpr sumLeader = LinearExpr.sum(leaderVars.toArray(new BoolVar[0]));
            LinearExpr sumFollower = LinearExpr.sum(followerVars.toArray(new BoolVar[0]));

            model.addEquality(sumLeader, sumFollower);
          }
          // Agar birida joy bo'lmasa (masalan xona yo'q), ikkinchisi ham qo'yilmasin
          else if ((leaderVars == null || leaderVars.isEmpty())
              && (followerVars != null && !followerVars.isEmpty())) {
            for (BoolVar v : followerVars) model.addEquality(v, 0);
          } else if ((leaderVars != null && !leaderVars.isEmpty())
              && (followerVars == null || followerVars.isEmpty())) {
            for (BoolVar v : leaderVars) model.addEquality(v, 0);
          }
        }

        // Bi-weekly bo'lsa, Haftalari ham teng bo'lishi shart
        // Lekin biz VariableFactory da SYNC_ID uchun bitta weekVar yaratganmiz.
        // Demak, bu avtomatik hal bo'lgan! (Qo'shimcha kod shart emas).
      }
    }
  }
}
