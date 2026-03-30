package com.sarmich.timetable.service.solver;

import com.google.ortools.sat.*;
import com.sarmich.timetable.model.Response;
import com.sarmich.timetable.model.SolverResult;
import com.sarmich.timetable.model.response.CompanyResponse;
import com.sarmich.timetable.model.response.OrTLesson;
import com.sarmich.timetable.utils.Util;
import java.time.DayOfWeek;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class WarmStartService {
  private final ModelDataIndexer dataIndexer;
  private final VariableFactory variableFactory;
  private final ConstraintManager constraintManager;
  private final SolutionProcessor solutionProcessor;

  /**
   * Mavjud dars jadvalini optimallashtirish (Warm Start).
   *
   * @param savedSchedule Oldindan tuzilgan jadval (Hintlar uchun).
   * @param lessons Hozirgi dars talablari (OrTLesson formatida).
   * @param company Maktab ma'lumotlari.
   * @param applySoftConstraint Sozlamalar.
   */
  public SolverResult optimizeSchedule(
      List<Response> savedSchedule,
      List<OrTLesson> lessons,
      CompanyResponse company,
      ApplySoftConstraint applySoftConstraint) {

    log.info("Starting RE-OPTIMIZATION process based on an existing schedule...");

    // 1. Modelni qurish
    ModelData data = dataIndexer.indexData(lessons, company, Collections.emptyList());
    CpModel model = new CpModel();
    ModelVariables variables = variableFactory.createVariables(model, data);
    var objective = LinearExpr.newBuilder();

    constraintManager.applyAllConstraints(model, variables, data, objective, applySoftConstraint);
    model.minimize(objective);

    // 2. Hintlarni (ishoralarni) qo'shish
    addSolutionHints(model, savedSchedule, lessons, variables, data);

    // 3. Solverni ishga tushirish
    CpSolver solver = createSolverForOptimization();
    CpSolverStatus status = solver.solve(model);

    // 4. Natijani qayta ishlash
    return solutionProcessor.process(status, solver, variables, data);
  }

  private void addSolutionHints(
      CpModel model,
      List<Response> savedSchedule,
      List<OrTLesson> originalLessons,
      ModelVariables vars,
      ModelData data) {

    // OrTLesson ni ID bo'yicha Map ga o'tkazish (Tezkor qidiruv uchun)
    // Endi aniq ID bo'yicha ishlaymiz, bu Split darslar uchun xatosiz ishlaydi.
    Map<Integer, OrTLesson> lessonMap =
        originalLessons.stream().collect(Collectors.toMap(OrTLesson::id, l -> l));

    int assignmentHints = 0;
    int weekHints = 0;

    for (Response scheduledLesson : savedSchedule) {
      // 1. SavedSchedule dan LessonID ni olib, OriginalLesson ni topamiz.
      // Response obyektida lessonId bo'lishi SHART (SolutionProcessor da qo'shgan edik).
      if (scheduledLesson.getLessonId() == null) {
        continue; // Eski formatdagi data bo'lsa o'tkazib yuboramiz
      }

      OrTLesson originalLesson = lessonMap.get(scheduledLesson.getLessonId());

      if (originalLesson == null) {
        // Demak bu dars talablar ro'yxatidan o'chirilgan, unga hint berolmaymiz.
        continue;
      }

      // 2. Indekslarni olish
      Integer cIdx = data.getClassIdToIndex().get(originalLesson.classInfo().id());
      Integer tIdx = data.getTeacherIdToIndex().get(originalLesson.teacher().id());
      Integer sIdx = data.getSubjectIdToIndex().get(originalLesson.subject().id());
      int period = Util.getNotNull(originalLesson.period(), 1);
      int h_start =
          getHourIndex(scheduledLesson.getDay(), scheduledLesson.getHour(), data.getHoursPerDay());
      int lessonId = originalLesson.id();

      if (cIdx == null || tIdx == null || sIdx == null) continue;

      // 3. Variable Key yasash (Format: ..._l{lessonId})
      String varKey;
      if (data.isUseRooms()) {
        int r = (scheduledLesson.getRoom() != null) ? scheduledLesson.getRoom().id() : 0;
        if (r == 0) continue; // Xona kerak bo'lsa-yu, jadvalda yo'q bo'lsa

        varKey =
            String.format(
                "c%d_t%d_s%d_h%d_r%d_p%d_l%d", cIdx, tIdx, sIdx, h_start, r, period, lessonId);
      } else {
        // Xonasiz rejim (r=0)
        varKey =
            String.format(
                "c%d_t%d_s%d_h%d_r0_p%d_l%d", cIdx, tIdx, sIdx, h_start, period, lessonId);
      }

      // 4. Assignment Hint ("Bu dars shu vaqtda bo'lsin")
      BoolVar assignmentVar = vars.getAssignmentVars().get(varKey);
      if (assignmentVar != null) {
        model.addHint(assignmentVar, 1);
        assignmentHints++;

        // 5. Week Hint ("Bu dars A yoki B haftada bo'lsin")
        if (scheduledLesson.getWeekIndex() != null) {
          // ModelVariables dagi keshdan Hafta o'zgaruvchisini olamiz
          IntVar weekVar = vars.getLessonIdToWeekVar().get(lessonId);
          if (weekVar != null) {
            model.addHint(weekVar, scheduledLesson.getWeekIndex());
            weekHints++;
          }
        }
      }
    }
    log.info("Added {} assignment hints and {} week hints.", assignmentHints, weekHints);
  }

  private int getHourIndex(DayOfWeek day, int hourOfDay, int hoursPerDay) {
    int dayIndex = day.getValue() - 1;
    return dayIndex * hoursPerDay + (hourOfDay - 1);
  }

  private CpSolver createSolverForOptimization() {
    CpSolver solver = new CpSolver();
    solver.getParameters().setNumWorkers(Runtime.getRuntime().availableProcessors());
    solver.getParameters().setMaxTimeInSeconds(60.0);
    solver.getParameters().setLogSearchProgress(true);
    return solver;
  }
}
