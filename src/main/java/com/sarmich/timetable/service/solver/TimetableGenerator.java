package com.sarmich.timetable.service.solver;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;
import com.sarmich.timetable.exception.InvalidOperationException;
import com.sarmich.timetable.model.SolverResult;
import com.sarmich.timetable.model.response.CompanyResponse;
import com.sarmich.timetable.model.response.OrTLesson;
import com.sarmich.timetable.model.response.RoomResponse;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class TimetableGenerator {

  private final PreSolverValidator validator;
  private final ModelDataIndexer dataIndexer;
  private final VariableFactory variableFactory;
  private final ConstraintManager constraintManager;
  private final SolutionProcessor solutionProcessor;

  static {
    Loader.loadNativeLibraries();
  }

  // Xonalarsiz (oddiy) generatsiya
  public SolverResult generate(List<OrTLesson> lessons, CompanyResponse company) {
    return generate(lessons, Collections.emptyList(), company, createDefaultConstraintOptions());
  }

  // To'liq generatsiya
  public SolverResult generate(
      List<OrTLesson> lessons,
      List<RoomResponse> allRooms,
      CompanyResponse company,
      ApplySoftConstraint options) {

    log.info(
        "Starting generation. Lessons: {}, Rooms: {}, A/B Logic: {}",
        lessons.size(),
        allRooms.size(),
        options.getApplyWeekParity());

    // 1. Validatsiya (YANGILANGAN PreSolverValidator ishlaydi)
    List<String> validationErrors = validator.validate(lessons, company);
    if (!validationErrors.isEmpty()) {
      validationErrors.forEach(log::error);
      throw new InvalidOperationException(String.join(",", validationErrors));
      // return createEmptyResultWithUnscheduled(lessons);
    }

    // 2. Data Indexing (YANGILANGAN ModelDataIndexer ishlaydi)
    ModelData data = dataIndexer.indexData(lessons, company, allRooms);

    // 3. Variables (YANGILANGAN VariableFactory Bi-weekly variablelarni yaratadi)
    CpModel model = new CpModel();
    ModelVariables variables = variableFactory.createVariables(model, data);

    // 4. Constraints (YANGILANGAN Hard/Soft ConstraintProviderlar ishlaydi)
    var objective = LinearExpr.newBuilder();
    constraintManager.applyAllConstraints(model, variables, data, objective, options);

    // Decision Strategy: Prioritize scheduling longer lessons first
    // We want to fix the start times of the longest lessons early to reduce
    // fragmentation.
    List<IntVar> startVarsOrderedByDuration =
        data.getLessons().stream()
            .sorted(
                java.util.Comparator.comparingInt(OrTLesson::lessonCount)
                    .reversed()) // Longest first
            .map(l -> variables.getLessonStartVars().get(l.id()))
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());

    if (!startVarsOrderedByDuration.isEmpty()) {
      model.addDecisionStrategy(
          startVarsOrderedByDuration.toArray(new IntVar[0]),
          com.google.ortools.sat.DecisionStrategyProto.VariableSelectionStrategy.CHOOSE_FIRST,
          com.google.ortools.sat.DecisionStrategyProto.DomainReductionStrategy.SELECT_MIN_VALUE);
    }

    // Maqsad funksiyasini minimallashtirish
    model.minimize(objective);

    // 5. Solving
    CpSolver solver = createSolver();
    CpSolverStatus status = solver.solve(model);

    // 6. Processing (SolutionProcessor natijani yig'adi)
    return solutionProcessor.process(status, solver, variables, data);
  }

  private ApplySoftConstraint createDefaultConstraintOptions() {
    return ApplySoftConstraint.builder()
        .applySoftConstraint(true)
        .applyUnScheduledLessons(true)
        .applyUnScheduledLessonsPenalty(100)
        // Default holatda WeekParity (A/B balans) ni yoqish tavsiya etiladi
        .applyWeekParity(true)
        .applyWeekParityPenalty(20)
        .build();
  }

  private CpSolver createSolver() {
    CpSolver solver = new CpSolver();

    // PERFORMANCE OPTIMIZATION 1: Barcha CPU yadrolari ishlatilsin
    int numWorkers = Runtime.getRuntime().availableProcessors();
    solver.getParameters().setNumWorkers(numWorkers);

    // PERFORMANCE OPTIMIZATION 2: Vaqt chegarasi (sekundlarda)
    solver.getParameters().setMaxTimeInSeconds(300.0);

    // PERFORMANCE OPTIMIZATION 3: Qidiruv progressini log qilish
    solver.getParameters().setLogSearchProgress(true);

    // PERFORMANCE OPTIMIZATION 4: Linearizatsiya darajasi
    // Bu CP-SAT ning MIP relaxationdan foydalanishiga yordam beradi
    solver.getParameters().setLinearizationLevel(2);

    // PERFORMANCE OPTIMIZATION 5: Probing va preprocessing
    // Oldindan o'zgaruvchilarning mumkin bo'lgan qiymatlarini aniqlash
    solver.getParameters().setCpModelPresolve(true);

    // PERFORMANCE OPTIMIZATION 6: LNS (Large Neighborhood Search) yoqish
    // Bu birinchi yechim topilgandan keyin optimallashtirish uchun yaxshi
    // solver.getParameters().setUseLns(true);

    log.info("Solver configured with {} workers, 300s timeout, LNS enabled", numWorkers);
    return solver;
  }

  // Agar xatolik bo'lsa, bo'sh natija qaytarish
  // private SolverResult createEmptyResultWithUnscheduled(List<LessonResponse>
  // lessons) {
  // List<UnscheduledLesson> allUnscheduled =
  // lessons.stream()
  // .map(
  // req -> {
  // // Agar UnscheduledLesson klassiga o'zgartirish kiritmagan bo'lsangiz,
  // // bu yer o'zgarishsiz qoladi.
  // // Mantiqan to'g'ri: Talab bor, Reja = 0, Yetishmovchilik = Talab.
  // return new UnscheduledLesson(
  // req.classInfo(),
  // req.teacher(),
  // req.subject(),
  // req.rooms(),
  // req.lessonCount(),
  // 0,
  // req.lessonCount());
  // })
  // .collect(Collectors.toList());
  // return new SolverResult(Collections.emptyList(), allUnscheduled);
  // }
}
