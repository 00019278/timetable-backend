package com.sarmich.timetable.service.solver;

import com.google.ortools.sat.*;
import com.sarmich.timetable.model.response.OrTLesson;
import com.sarmich.timetable.model.response.TeacherResponse;
import com.sarmich.timetable.utils.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class SoftConstraintProvider {

  /** Entry point: Barcha yumshoq cheklovlarni tartib bilan qo'llash */
  public void apply(
      CpModel model,
      ModelVariables vars,
      ModelData data,
      LinearExprBuilder objective,
      ApplySoftConstraint options) {

    log.info("Applying soft constraints...");

    // 1. Rejalashtirilmagan darslar
    if (Boolean.TRUE.equals(options.getApplyUnScheduledLessons())) {
      addUnscheduledLessonPenalty(
          objective, vars, data, options.getApplyUnScheduledLessonsPenalty());
    }

    // 2. O'qituvchilar continuity (Gaps)
    if (Boolean.TRUE.equals(options.getApplyContinuityPenaltyTeacher())) {
      addContinuityPenaltyTeacher(
          model, objective, vars, data, options.getApplyContinuityPenaltyTeacherPenalty());
    }

    // 3. Sinflar continuity (Gaps)
    if (Boolean.TRUE.equals(options.getApplyContinuityPenaltyClass())) {
      addContinuityPenaltyClass(
          model, objective, vars, data, options.getApplyContinuityPenaltyClassPenalty());
    }

    // 4. Kunlik yuklama balansi (A va B haftalar uchun alohida)
    if (Boolean.TRUE.equals(options.getApplyBalancedLoad())) {
      addBalancedWeightedLoadPenalty(
          model, objective, vars, data, options.getApplyBalancedLoadPenalty());
    }

    // 5. Kunlik fanlar taqsimoti (Daily Distribution)
    if (Boolean.TRUE.equals(options.getApplyDailySubjectDistribution())) {
      addDailySubjectDistributionPenalty(
          model, objective, vars, data, options.getApplyDailySubjectDistributionPenalty());
    }

    // 6. Haftalik sinxronlash (Week Parity)
    if (Boolean.TRUE.equals(options.getApplyWeekParity())) {
      int penalty =
          options.getApplyWeekParityPenalty() != null ? options.getApplyWeekParityPenalty() : 20;
      addWeekParityPenalty(model, objective, vars, data, penalty);
    }

    // 7. Teacher Bi-Weekly Load Balance
    addTeacherBiWeeklyLoadBalance(model, objective, vars, data, 10);
  }

  // ============================================================================================
  // 1. UNSCHEDULED LESSONS
  // ============================================================================================
  private void addUnscheduledLessonPenalty(
      LinearExprBuilder objective, ModelVariables vars, ModelData data, Integer penalty) {
    log.info("Adding penalty for unscheduled lessons...");

    // LOOP TYPE CHANGED: LessonResponse -> OrTLesson
    for (OrTLesson lesson : data.getLessons()) {
      Integer cIdx = data.getClassIdToIndex().get(lesson.classInfo().id());
      Integer tIdx = data.getTeacherIdToIndex().get(lesson.teacher().id());
      Integer sIdx = data.getSubjectIdToIndex().get(lesson.subject().id());
      int weeklyCount = lesson.lessonCount();
      int period = Util.getNotNull(lesson.period(), 1);

      if (cIdx == null || tIdx == null || sIdx == null || weeklyCount == 0) continue;

      List<BoolVar> allPossibleBlocks =
          vars.getLessonsByClassTeacherSubject().get(cIdx + "_" + tIdx + "_" + sIdx);

      if (allPossibleBlocks == null || allPossibleBlocks.isEmpty()) {
        objective.addTerm(LinearExpr.constant(weeklyCount), penalty);
        continue;
      }
      LinearExprBuilder scheduledHoursBuilder = LinearExpr.newBuilder();
      for (BoolVar blockVar : allPossibleBlocks) {
        scheduledHoursBuilder.addTerm(blockVar, period);
      }
      LinearExpr scheduledHours = scheduledHoursBuilder.build();
      objective.addTerm(LinearExpr.constant(weeklyCount), penalty);
      objective.addTerm(scheduledHours, -penalty);
    }
  }

  // ============================================================================================
  // 2. TEACHER CONTINUITY (GAPS)
  // ============================================================================================
  private void addContinuityPenaltyTeacher(
      CpModel model,
      LinearExprBuilder objective,
      ModelVariables vars,
      ModelData data,
      Integer penalty) {
    log.info("Adding gap penalties for TEACHERS...");
    int hoursPerDay = data.getHoursPerDay();
    int days = data.getDays();
    int teacherCount = data.getUniqueTeachers().size();

    for (int t = 0; t < teacherCount; t++) {
      for (int d = 0; d < days; d++) {
        BoolVar[] existsA =
            getDailyExistenceVarsForWeek(
                model,
                vars,
                t,
                d,
                hoursPerDay,
                vars.getLessonsByTeacherHour(),
                0,
                "t" + t + "_d" + d + "_wA");
        addGapsPenaltyForEntity(model, objective, existsA, "t" + t + "_d" + d + "_gapA", penalty);

        BoolVar[] existsB =
            getDailyExistenceVarsForWeek(
                model,
                vars,
                t,
                d,
                hoursPerDay,
                vars.getLessonsByTeacherHour(),
                1,
                "t" + t + "_d" + d + "_wB");
        addGapsPenaltyForEntity(model, objective, existsB, "t" + t + "_d" + d + "_gapB", penalty);
      }
    }
  }

  // ============================================================================================
  // 3. CLASS CONTINUITY (GAPS)
  // ============================================================================================
  private void addContinuityPenaltyClass(
      CpModel model,
      LinearExprBuilder objective,
      ModelVariables vars,
      ModelData data,
      Integer penalty) {
    log.info("Adding gap penalties for CLASSES...");
    int hoursPerDay = data.getHoursPerDay();
    int days = data.getDays();
    int classCount = data.getUniqueClasses().size();

    for (int c = 0; c < classCount; c++) {
      for (int d = 0; d < days; d++) {
        BoolVar[] existsA =
            getDailyExistenceVarsForWeek(
                model,
                vars,
                c,
                d,
                hoursPerDay,
                vars.getLessonsByClassHour(),
                0,
                "c" + c + "_d" + d + "_wA");
        addGapsPenaltyForEntity(model, objective, existsA, "c" + c + "_d" + d + "_gapA", penalty);

        BoolVar[] existsB =
            getDailyExistenceVarsForWeek(
                model,
                vars,
                c,
                d,
                hoursPerDay,
                vars.getLessonsByClassHour(),
                1,
                "c" + c + "_d" + d + "_wB");
        addGapsPenaltyForEntity(model, objective, existsB, "c" + c + "_d" + d + "_gapB", penalty);
      }
    }
  }

  // ============================================================================================
  // 4. BALANCED WEIGHTED LOAD
  // ============================================================================================
  private void addBalancedWeightedLoadPenalty(
      CpModel model,
      LinearExprBuilder objective,
      ModelVariables vars,
      ModelData data,
      Integer penalty) {
    log.info("Adding weighted load balance penalty (Layered A/B)...");

    for (Map.Entry<Integer, Integer> entry : data.getClassIdToIndex().entrySet()) {
      Integer classId = entry.getKey();
      Integer cIdx = entry.getValue();

      // STREAM TYPE CHANGED: OrTLesson
      double totalWeight =
          data.getLessons().stream()
              .filter(l -> l.classInfo().id().equals(classId))
              .mapToDouble(
                  l -> {
                    Integer sIdx = data.getSubjectIdToIndex().get(l.subject().id());
                    int w =
                        (sIdx != null) ? data.getSubjectIndexToWeight().getOrDefault(sIdx, 1) : 1;
                    return l.lessonCount() * w;
                  })
              .sum();

      if (totalWeight == 0) continue;
      long avgDaily = Math.round(totalWeight / data.getDays());

      LinearExpr[] dailyLoadsA = new LinearExpr[data.getDays()];
      LinearExpr[] dailyLoadsB = new LinearExpr[data.getDays()];

      for (int d = 0; d < data.getDays(); d++) {
        LinearExprBuilder sumA = LinearExpr.newBuilder();
        LinearExprBuilder sumB = LinearExpr.newBuilder();
        int startH = d * data.getHoursPerDay();
        int endH = startH + data.getHoursPerDay();

        for (int h = startH; h < endH; h++) {
          List<BoolVar> slots = vars.getLessonsByClassHour().get(cIdx + "_" + h);
          if (slots == null) continue;
          for (BoolVar lessonVar : slots) {
            int h_start = Integer.parseInt(lessonVar.getName().split("_")[3].substring(1));
            if (h == h_start) {
              int sIdx = Integer.parseInt(lessonVar.getName().split("_")[2].substring(1));
              int weight = data.getSubjectIndexToWeight().getOrDefault(sIdx, 1);
              int period = extractPeriodFromVarName(lessonVar.getName());
              long loadVal = (long) weight * period;

              IntVar weekVar = vars.getLessonWeekVars().get(lessonVar.getName());
              if (weekVar == null) {
                sumA.addTerm(lessonVar, loadVal);
                sumB.addTerm(lessonVar, loadVal);
              } else {
                BoolVar isW0 = model.newBoolVar(lessonVar.getName() + "_isW0_load");
                model.addEquality(weekVar, 0).onlyEnforceIf(isW0);
                model.addDifferent(weekVar, 0).onlyEnforceIf(isW0.not());

                BoolVar activeA = model.newBoolVar(lessonVar.getName() + "_actA_load");
                model.addBoolAnd(new Literal[] {lessonVar, isW0}).onlyEnforceIf(activeA);
                sumA.addTerm(activeA, loadVal);

                BoolVar activeB = model.newBoolVar(lessonVar.getName() + "_actB_load");
                model.addBoolAnd(new Literal[] {lessonVar, isW0.not()}).onlyEnforceIf(activeB);
                sumB.addTerm(activeB, loadVal);
              }
            }
          }
        }
        dailyLoadsA[d] = sumA.build();
        dailyLoadsB[d] = sumB.build();
      }

      int maxW = (int) (avgDaily * 3);
      for (int d = 0; d < data.getDays(); d++) {
        IntVar devA = model.newIntVar(0, maxW, "balA_c" + cIdx + "_d" + d);
        model.addGreaterOrEqual(devA, LinearExpr.newBuilder().add(dailyLoadsA[d]).add(-avgDaily));
        model.addGreaterOrEqual(
            devA, LinearExpr.newBuilder().addTerm(dailyLoadsA[d], -1).add(avgDaily));
        objective.addTerm(devA, penalty);

        IntVar devB = model.newIntVar(0, maxW, "balB_c" + cIdx + "_d" + d);
        model.addGreaterOrEqual(devB, LinearExpr.newBuilder().add(dailyLoadsB[d]).add(-avgDaily));
        model.addGreaterOrEqual(
            devB, LinearExpr.newBuilder().addTerm(dailyLoadsB[d], -1).add(avgDaily));
        objective.addTerm(devB, penalty);
      }
    }
  }

  // ============================================================================================
  // 5. DAILY SUBJECT DISTRIBUTION
  // ============================================================================================
  private void addDailySubjectDistributionPenalty(
      CpModel model,
      LinearExprBuilder objective,
      ModelVariables vars,
      ModelData data,
      Integer penalty) {
    log.info("Adding daily subject distribution penalty (Layered A/B)...");

    // LOOP TYPE CHANGED: OrTLesson
    for (OrTLesson lesson : data.getLessons()) {
      if (lesson.lessonCount() >= 4) continue;

      Integer cIdx = data.getClassIdToIndex().get(lesson.classInfo().id());
      Integer tIdx = data.getTeacherIdToIndex().get(lesson.teacher().id());
      Integer sIdx = data.getSubjectIdToIndex().get(lesson.subject().id());
      if (cIdx == null || tIdx == null || sIdx == null) continue;

      List<BoolVar> allBlocks =
          vars.getLessonsByClassTeacherSubject().get(cIdx + "_" + tIdx + "_" + sIdx);
      if (allBlocks == null || allBlocks.isEmpty()) continue;

      for (int day = 0; day < data.getDays(); day++) {
        List<BoolVar> dailyBlocks = new ArrayList<>();
        int startHour = day * data.getHoursPerDay();
        int endHour = startHour + data.getHoursPerDay();

        for (BoolVar var : allBlocks) {
          int h_start = Integer.parseInt(var.getName().split("_")[3].substring(1));
          if (h_start >= startHour && h_start < endHour) {
            dailyBlocks.add(var);
          }
        }

        if (dailyBlocks.size() > 1) {
          LinearExpr sumA = calculateSumForWeek(model, vars, dailyBlocks, 0);
          LinearExpr sumB = calculateSumForWeek(model, vars, dailyBlocks, 1);

          IntVar overflowA =
              model.newIntVar(0, dailyBlocks.size(), "dist_ovfA_" + lesson.id() + "_d" + day);
          model.addGreaterOrEqual(overflowA, LinearExpr.newBuilder().add(sumA).add(-1));

          IntVar overflowB =
              model.newIntVar(0, dailyBlocks.size(), "dist_ovfB_" + lesson.id() + "_d" + day);
          model.addGreaterOrEqual(overflowB, LinearExpr.newBuilder().add(sumB).add(-1));

          objective.addTerm(overflowA, penalty);
          objective.addTerm(overflowB, penalty);
        }
      }
    }
  }

  // ============================================================================================
  // 6. WEEK PARITY (SYNC)
  // ============================================================================================
  private void addWeekParityPenalty(
      CpModel model,
      LinearExprBuilder objective,
      ModelVariables vars,
      ModelData data,
      Integer penalty) {
    log.info("Adding Week Parity Penalty (filling A/B gaps) with weight: {}", penalty);
    for (Map.Entry<Integer, Integer> entry : data.getClassIdToIndex().entrySet()) {
      Integer cIdx = entry.getValue();

      for (int h = 0; h < data.getHoursCount(); h++) {
        String classHourKey = cIdx + "_" + h;
        List<BoolVar> potentialLessons = vars.getLessonsByClassHour().get(classHourKey);
        if (potentialLessons == null || potentialLessons.isEmpty()) continue;

        List<Literal> activeInWeekA = new ArrayList<>();
        List<Literal> activeInWeekB = new ArrayList<>();

        for (BoolVar lessonVar : potentialLessons) {
          IntVar weekVar = vars.getLessonWeekVars().get(lessonVar.getName());
          if (weekVar == null) continue;

          BoolVar isWeek0 = model.newBoolVar(lessonVar.getName() + "_isW0_chk");
          model.addEquality(weekVar, 0).onlyEnforceIf(isWeek0);
          model.addDifferent(weekVar, 0).onlyEnforceIf(isWeek0.not());

          BoolVar activeA = model.newBoolVar(lessonVar.getName() + "_actA");
          model.addBoolAnd(new Literal[] {lessonVar, isWeek0}).onlyEnforceIf(activeA);
          model.addImplication(activeA, lessonVar);
          activeInWeekA.add(activeA);

          BoolVar activeB = model.newBoolVar(lessonVar.getName() + "_actB");
          model.addBoolAnd(new Literal[] {lessonVar, isWeek0.not()}).onlyEnforceIf(activeB);
          model.addImplication(activeB, lessonVar);
          activeInWeekB.add(activeB);
        }

        if (activeInWeekA.isEmpty() && activeInWeekB.isEmpty()) continue;

        LinearExpr sumA = LinearExpr.sum(activeInWeekA.toArray(new Literal[0]));
        LinearExpr sumB = LinearExpr.sum(activeInWeekB.toArray(new Literal[0]));

        IntVar imbalance = model.newIntVar(0, potentialLessons.size(), "imb_c" + cIdx + "_h" + h);
        model.addGreaterOrEqual(imbalance, LinearExpr.newBuilder().add(sumA).addTerm(sumB, -1));
        model.addGreaterOrEqual(imbalance, LinearExpr.newBuilder().add(sumB).addTerm(sumA, -1));

        objective.addTerm(imbalance, penalty);
      }
    }
  }

  // ============================================================================================
  // 7. TEACHER BI-WEEKLY LOAD BALANCE
  // ============================================================================================
  private void addTeacherBiWeeklyLoadBalance(
      CpModel model,
      LinearExprBuilder objective,
      ModelVariables vars,
      ModelData data,
      Integer penalty) {
    log.info("Adding teacher bi-weekly load balance penalty...");

    for (TeacherResponse teacher : data.getUniqueTeachers()) {
      Integer tIdx = data.getTeacherIdToIndex().get(teacher.id());
      List<Literal> loadA = new ArrayList<>();
      List<Literal> loadB = new ArrayList<>();

      for (int h = 0; h < data.getHoursCount(); h++) {
        List<BoolVar> lessons = vars.getLessonsByTeacherHour().get(tIdx + "_" + h);
        if (lessons == null) continue;

        for (BoolVar var : lessons) {
          IntVar weekVar = vars.getLessonWeekVars().get(var.getName());
          if (weekVar != null) {
            BoolVar isW0 = model.newBoolVar(var.getName() + "_isTchW0");
            model.addEquality(weekVar, 0).onlyEnforceIf(isW0);
            model.addDifferent(weekVar, 0).onlyEnforceIf(isW0.not());

            BoolVar actA = model.newBoolVar(var.getName() + "_tacA");
            model.addBoolAnd(new Literal[] {var, isW0}).onlyEnforceIf(actA);
            model.addImplication(actA, var);
            loadA.add(actA);

            BoolVar actB = model.newBoolVar(var.getName() + "_tacB");
            model.addBoolAnd(new Literal[] {var, isW0.not()}).onlyEnforceIf(actB);
            model.addImplication(actB, var);
            loadB.add(actB);
          }
        }
      }

      if (loadA.isEmpty() && loadB.isEmpty()) continue;

      LinearExpr sumA = LinearExpr.sum(loadA.toArray(new Literal[0]));
      LinearExpr sumB = LinearExpr.sum(loadB.toArray(new Literal[0]));

      IntVar diff = model.newIntVar(0, data.getHoursCount(), "tch_bal_" + tIdx);
      model.addGreaterOrEqual(diff, LinearExpr.newBuilder().add(sumA).addTerm(sumB, -1));
      model.addGreaterOrEqual(diff, LinearExpr.newBuilder().add(sumB).addTerm(sumA, -1));

      objective.addTerm(diff, penalty);
    }
  }

  // ============================================================================================
  // YORDAMCHI METODLAR (HELPERS)
  // ============================================================================================

  private BoolVar[] getDailyExistenceVarsForWeek(
      CpModel model,
      ModelVariables vars,
      int entityIndex,
      int dayIndex,
      int hoursPerDay,
      Map<String, List<BoolVar>> lessonsByEntityHour,
      int targetWeek,
      String prefix) {

    BoolVar[] result = new BoolVar[hoursPerDay];
    int startHour = dayIndex * hoursPerDay;

    for (int h = 0; h < hoursPerDay; h++) {
      int globalHour = startHour + h;
      String key = entityIndex + "_" + globalHour;
      List<BoolVar> lessons = lessonsByEntityHour.getOrDefault(key, new ArrayList<>());
      List<Literal> activeInTargetWeek = new ArrayList<>();

      for (BoolVar var : lessons) {
        IntVar weekVar = vars.getLessonWeekVars().get(var.getName());
        if (weekVar == null) {
          activeInTargetWeek.add(var);
        } else {
          BoolVar isTarget = model.newBoolVar(prefix + "_tg_" + var.getName());
          model.addEquality(weekVar, targetWeek).onlyEnforceIf(isTarget);
          model.addDifferent(weekVar, targetWeek).onlyEnforceIf(isTarget.not());

          BoolVar active = model.newBoolVar(prefix + "_ac_" + var.getName());
          model.addBoolAnd(new Literal[] {var, isTarget}).onlyEnforceIf(active);
          model.addImplication(active, var);
          activeInTargetWeek.add(active);
        }
      }
      result[h] = createLessonExistenceVar(model, prefix + "_exist_h" + h, activeInTargetWeek);
    }
    return result;
  }

  private BoolVar createLessonExistenceVar(CpModel model, String name, List<Literal> literals) {
    BoolVar existenceVar = model.newBoolVar(name);
    if (literals.isEmpty()) {
      model.addEquality(existenceVar, 0);
    } else {
      model.addBoolOr(literals.toArray(new Literal[0])).onlyEnforceIf(existenceVar);
      for (Literal l : literals) {
        model.addImplication(l, existenceVar);
      }
    }
    return existenceVar;
  }

  private void addGapsPenaltyForEntity(
      CpModel model,
      LinearExprBuilder objective,
      BoolVar[] lessonExists,
      String namePrefix,
      int penalty) {
    for (int h = 0; h < lessonExists.length - 2; h++) {
      BoolVar gapVar = model.newBoolVar(namePrefix + "_gap_" + (h + 1));
      model
          .addBoolAnd(
              new Literal[] {lessonExists[h], lessonExists[h + 1].not(), lessonExists[h + 2]})
          .onlyEnforceIf(gapVar);
      objective.addTerm(gapVar, penalty);
    }
  }

  private LinearExpr calculateSumForWeek(
      CpModel model, ModelVariables vars, List<BoolVar> blocks, int targetWeek) {
    LinearExprBuilder sum = LinearExpr.newBuilder();
    for (BoolVar var : blocks) {
      IntVar weekVar = vars.getLessonWeekVars().get(var.getName());
      if (weekVar == null) {
        sum.addTerm(var, 1);
      } else {
        BoolVar isTarget = model.newBoolVar(var.getName() + "_isW" + targetWeek + "_dist");
        model.addEquality(weekVar, targetWeek).onlyEnforceIf(isTarget);
        model.addDifferent(weekVar, targetWeek).onlyEnforceIf(isTarget.not());

        BoolVar active = model.newBoolVar(var.getName() + "_actW" + targetWeek + "_dist");
        model.addBoolAnd(new Literal[] {var, isTarget}).onlyEnforceIf(active);
        sum.addTerm(active, 1);
      }
    }
    return sum.build();
  }

  private int extractPeriodFromVarName(String varName) {
    String[] parts = varName.split("_");
    for (int i = parts.length - 1; i >= 0; i--) {
      if (parts[i].startsWith("p")) {
        try {
          return Integer.parseInt(parts[i].substring(1));
        } catch (NumberFormatException e) {
          return 1;
        }
      }
    }
    return 1;
  }
}
