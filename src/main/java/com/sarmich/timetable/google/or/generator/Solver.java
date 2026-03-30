package com.sarmich.timetable.google.or.generator;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import com.sarmich.timetable.google.or.models.DemoData;
import com.sarmich.timetable.google.or.models.Lesson;
import com.sarmich.timetable.google.or.models.Response;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Solver {
  public List<Response> generate(List<Lesson> lessons) {
    log.info("Start :  {}", LocalDateTime.now());
    // Class
    List<Integer> classList =
        lessons.stream().map(Lesson::getClassId).collect(Collectors.toSet()).stream().toList();
    HashMap<Integer, Integer> classIdMap = new HashMap<>();
    HashMap<Integer, Integer> classIndexMap = new HashMap<>();

    for (int i = 0; i < classList.size(); i++) {
      classIdMap.put(classList.get(i), i);
      classIndexMap.put(i, classList.get(i));
    }

    // Subject
    List<Integer> subjectList =
        lessons.stream().map(Lesson::getSubjectId).collect(Collectors.toSet()).stream().toList();
    HashMap<Integer, Integer> subjectIdMap = new HashMap<>();
    HashMap<Integer, Integer> subjectIndexMap = new HashMap<>();
    for (int i = 0; i < subjectList.size(); i++) {
      subjectIdMap.put(subjectList.get(i), i);
      subjectIndexMap.put(i, subjectList.get(i));
    }

    // Teacher
    List<Integer> teachers =
        lessons.stream().map(Lesson::getTeacherId).collect(Collectors.toSet()).stream().toList();
    HashMap<Integer, Integer> teacherIdMap = new HashMap<>();
    HashMap<Integer, Integer> teacherIndexMap = new HashMap<>();
    for (int i = 0; i < teachers.size(); i++) {
      teacherIndexMap.put(i, teachers.get(i));
      teacherIdMap.put(teachers.get(i), i);
    }

    Loader.loadNativeLibraries();
    CpModel model = new CpModel();

    int roomCount = 40;
    int hoursCount = 42;

    BoolVar[][][][][] timetable =
        createTimetableVariablesWithClassClone(
            model, hoursCount, classIndexMap, roomCount, teacherIdMap, subjectIdMap, lessons);
    // Add constraints
    addConstraints(
        model, timetable, classIndexMap, teacherIndexMap, subjectIndexMap, roomCount, hoursCount);

    //     Subject weekly constraints
    addWeeklyCount(
        model,
        timetable,
        classIndexMap,
        subjectIndexMap,
        teacherIndexMap,
        roomCount,
        hoursCount,
        lessons);
    int subjectsCount = subjectList.size(); // subjectsCount ni shu yerda olamiz
    LinearExprBuilder globalObjective = LinearExpr.newBuilder();

    addSoftConstraintsForContinuity(
        model,
        timetable,
        classIndexMap,
        teacherIndexMap,
        roomCount,
        hoursCount,
        subjectsCount,
        globalObjective);
    // ===============================================
    // 2. Kunlik yuklamani tenglashtirish (YANGI METOD)
    addSoftConstraintsForBalancedDailyLoad(
        model,
        timetable,
        classIndexMap,
        teacherIndexMap,
        subjectIndexMap,
        roomCount,
        hoursCount,
        lessons,
        globalObjective // <-- Shu obyektga o'z jarimalarini qo'shadi
        );

    // Solverga umumiy jarimani minimallashtirishni buyuramiz
    model.minimize(globalObjective);
    return solveAndGenerateTimetable(
        model, hoursCount, subjectIndexMap, teacherIndexMap, classIndexMap, timetable, roomCount);
  }

  private void addSoftConstraintsForBalancedDailyLoad(
      CpModel model,
      BoolVar[][][][][] timetable,
      HashMap<Integer, Integer> classIndexMap,
      HashMap<Integer, Integer> teacherIndexMap,
      HashMap<Integer, Integer> subjectIndexMap,
      int roomCount,
      int hoursCount,
      List<Lesson> lessons,
      LinearExprBuilder objective) { // Mavjud objective'ni parametr sifatida olamiz

    int dailyHours = 7; // Kunlik umumiy slotlar soni
    int daysCount = hoursCount / dailyHours;
    final int overloadPenalty =
        5; // Maksimal soatdan oshgani uchun jarima (anchagina katta bo'lgani ma'qul)

    for (int c = 0; c < classIndexMap.size(); c++) {
      int classId = classIndexMap.get(c);

      // 1. Sinfning umumiy haftalik yuklamasini hisoblash
      int totalWeeklyHours =
          lessons.stream()
              .filter(l -> Objects.equals(l.getClassId(), classId))
              .mapToInt(Lesson::getCount)
              .sum();

      // 2. Kunlik maksimal chegarani belgilash
      // Masalan: o'rtacha 5 bo'lsa, max 6. O'rtacha 4 bo'lsa, max 5.
      // Oddiy formula: O'rtacha + 1 (yoki qat'iy 6 deb olish ham mumkin)
      int avgDaily = (int) Math.ceil((double) totalWeeklyHours / daysCount);
      int maxDailyLoad = Math.min(avgDaily + 1, dailyHours); // 7 soatdan oshib ketmasin

      // Agar umumiy soat juda kam bo'lsa, bu cheklov shart emas.
      if (totalWeeklyHours == 0) continue;

      // Har bir kun uchun tekshiramiz
      for (int day = 0; day < daysCount; day++) {
        int startHour = day * dailyHours;
        int endHour = startHour + dailyHours;

        // Shu kundagi sinfning barcha darslarini yig'amiz
        List<BoolVar> dailyLessons = new ArrayList<>();
        for (int h = startHour; h < endHour; h++) {
          for (int r = 0; r < roomCount; r++) {
            for (int t = 0; t < teacherIndexMap.size(); t++) {
              for (int s = 0; s < subjectIndexMap.size(); s++) {
                if (timetable[c][r][t][h][s] != null) {
                  dailyLessons.add(timetable[c][r][t][h][s]);
                }
              }
            }
          }
        }

        if (dailyLessons.isEmpty()) continue; // Bu kuni dars yo'q

        // KUNLIK DARSLAR YIG'INDISI (LinearExpr)
        LinearExpr actualDailyLoad = LinearExpr.sum(dailyLessons.toArray(new BoolVar[0]));

        // 3. Jarimani hisoblash: Jarima = max(0, actualDailyLoad - maxDailyLoad)
        // Buning uchun yangi o'zgaruvchi 'overload' kerak.
        IntVar overload =
            model.newIntVar(0, dailyHours, "class_" + c + "_day_" + day + "_overload");

        // Cheklov: overload >= actualDailyLoad - maxDailyLoad
        // Buni quyidagicha yozamiz: overload - actualDailyLoad >= -maxDailyLoad
        // Yoki: actualDailyLoad - overload <= maxDailyLoad
        model.addLessOrEqual(
            LinearExpr.newBuilder().add(actualDailyLoad).addTerm(overload, -1), maxDailyLoad);

        // Solver overloadni minimallashtirishga harakat qiladi (chunki u objective ga qo'shiladi)
        // Shuning uchun u manfiy bo'lmaydi (pastki chegara 0) va kerakidan katta bo'lmaydi.

        // Jarimani umumiy maqsad funksiyasiga qo'shamiz
        objective.addTerm(overload, overloadPenalty);
      }
    }
  }

  private void addWeeklyCount(
      CpModel model,
      BoolVar[][][][][] timetable,
      HashMap<Integer, Integer> classIndexMap,
      HashMap<Integer, Integer> subjectIndexMap,
      HashMap<Integer, Integer> teacherIndexMap,
      int roomCount,
      Integer hourCount,
      List<Lesson> lesson) {

    for (int c = 0; c < classIndexMap.size(); c++) {
      for (int s = 0; s < subjectIndexMap.size(); s++) {
        int subjectId = subjectIndexMap.get(s);
        int classId = classIndexMap.get(c);

        // Haftada nechta soat o‘tilishi kerakligini olish (DemoData yoki DB orqali)
        Optional<Lesson> first =
            lesson.stream()
                .filter(l -> l.getClassId().equals(classId) && l.getSubjectId().equals(subjectId))
                .findFirst();
        int weeklyCount = first.isPresent() ? first.get().getCount() : 0;

        // Umumiy count (hafta bo‘yicha jami soat) == weeklyCount
        List<BoolVar> subjectOccurrences = new ArrayList<>();
        for (int r = 0; r < roomCount; r++) {
          for (int t = 0; t < teacherIndexMap.size(); t++) {
            for (int h = 0; h < hourCount; h++) {
              if (timetable[c][r][t][h][s] != null) {
                subjectOccurrences.add(timetable[c][r][t][h][s]);
              }
            }
          }
        }
        model.addEquality(LinearExpr.sum(subjectOccurrences.toArray(new BoolVar[0])), weeklyCount);

        // 🔹 1-kunlik cheklovlar
        for (DayOfWeek day : DayOfWeek.values()) {
          // bu kunga tegishli soatlar oralig‘i
          int startHour = (day.getValue() - 1) * 7; // masalan MONDAY = 0..6
          int endHour = startHour + 7; // 7 soat/day

          List<BoolVar> dailyOccurrences = new ArrayList<>();
          for (int r = 0; r < roomCount; r++) {
            for (int t = 0; t < teacherIndexMap.size(); t++) {
              for (int h = startHour; h < endHour && h < hourCount; h++) {
                if (timetable[c][r][t][h][s] != null) {
                  dailyOccurrences.add(timetable[c][r][t][h][s]);
                }
              }
            }
          }

          if (weeklyCount > 6) {
            model.addLessOrEqual(LinearExpr.sum(dailyOccurrences.toArray(new BoolVar[0])), 2);
          } else {
            model.addLessOrEqual(LinearExpr.sum(dailyOccurrences.toArray(new BoolVar[0])), 1);
          }
        }
      }
    }
  }

  private BoolVar[][][][][] createTimetableVariablesWithClassClone(
      CpModel model,
      Integer hourCount,
      HashMap<Integer, Integer> classIndexMap,
      int roomCount,
      HashMap<Integer, Integer> teacherMap,
      HashMap<Integer, Integer> subjectMap,
      List<Lesson> lessons) {
    //      Class c uchun, Room rda, Teacher t bilan, Hour h da, Subject s qo‘yildimi yoki yo‘qmi?
    int classCount = lessons.stream().map(Lesson::getClassId).collect(Collectors.toSet()).size();
    int teacherCount =
        lessons.stream().map(Lesson::getTeacherId).collect(Collectors.toSet()).size();
    int subjectCount =
        lessons.stream().map(Lesson::getSubjectId).collect(Collectors.toSet()).size();

    BoolVar[][][][][] timetable =
        new BoolVar[classCount][roomCount][teacherCount][hourCount][subjectCount];
    for (int r = 0; r < roomCount; r++) {
      for (int h = 0; h < hourCount; h++) {
        for (int c = 0; c < classIndexMap.size(); c++) {
          Integer classId = classIndexMap.get(c);
          List<Integer> teachers =
              lessons.stream()
                  .filter(l -> Objects.equals(l.getClassId(), classId))
                  .collect(Collectors.toSet())
                  .stream()
                  .map(Lesson::getTeacherId)
                  .toList();
          for (int teacherId : teachers) {
            Integer t = teacherMap.get(teacherId);
            List<Integer> subjects =
                lessons.stream()
                    .filter(
                        l ->
                            Objects.equals(l.getClassId(), classId)
                                && Objects.equals(l.getTeacherId(), teacherId))
                    .collect(Collectors.toSet())
                    .stream()
                    .map(Lesson::getSubjectId)
                    .toList();
            for (int subjectId : subjects) {
              int s = subjectMap.get(subjectId);
              String str = "c" + c + "_r" + r + "_t" + t + "_h" + h + "_s" + s;
              timetable[c][r][t][h][s] = model.newBoolVar(str);
            }
          }
        }
      }
    }
    return timetable;
  }

  private void addConstraints(
      CpModel model,
      BoolVar[][][][][] timetable,
      HashMap<Integer, Integer> classIndexMap,
      HashMap<Integer, Integer> teacherIndexMap,
      HashMap<Integer, Integer> subjectIndexMap,
      int roomCount,
      int hourCount) {
    // Add constraints to the model
    addConstraintsToTeacher(
        model, timetable, classIndexMap, teacherIndexMap, subjectIndexMap, roomCount, hourCount);

    // har bir room uchun
    addRoomConstraints(
        model, timetable, classIndexMap, teacherIndexMap, subjectIndexMap, roomCount, hourCount);
    // har bir class uchun
    addClassConstraints(
        model, timetable, classIndexMap, teacherIndexMap, subjectIndexMap, roomCount, hourCount);
  }

  private void addClassConstraints(
      CpModel model,
      BoolVar[][][][][] timetable,
      HashMap<Integer, Integer> classIndexMap,
      HashMap<Integer, Integer> teacherIndexMap,
      HashMap<Integer, Integer> subjectIndexMap,
      int roomCount,
      int hourCount) {

    // Class constraint: har bir class har bir soatda faqat 1ta dars oladi
    for (int c = 0; c < classIndexMap.size(); c++) { // har bir class
      for (int h = 0; h < hourCount; h++) { // har bir hour
        List<BoolVar> classLessonsAtHour = new ArrayList<>();
        for (int r = 0; r < roomCount; r++) { // barcha xonalar
          for (int t = 0; t < teacherIndexMap.size(); t++) { // barcha o‘qituvchilar
            for (int s = 0; s < subjectIndexMap.size(); s++) { // barcha fanlar
              if (timetable[c][r][t][h][s] != null) {
                classLessonsAtHour.add(timetable[c][r][t][h][s]);
              }
            }
          }
        }
        // Shu class uchun shu soatda faqat 1ta dars bo'lishi mumkin
        model.addAtMostOne(classLessonsAtHour.toArray(new BoolVar[0]));
      }
    }
  }

  private void addRoomConstraints(
      CpModel model,
      BoolVar[][][][][] timetable,
      HashMap<Integer, Integer> classIndexMap,
      HashMap<Integer, Integer> teacherIndexMap,
      HashMap<Integer, Integer> subjectIndexMap,
      int roomCount,
      int hourCount) {

    // Room constraint: har bir room har bir soatda faqat bitta darsga ajratiladi
    for (int r = 0; r < roomCount; r++) { // har bir room
      for (int h = 0; h < hourCount; h++) { // har bir hour
        List<BoolVar> roomLessonsAtHour = new ArrayList<>();
        for (int c = 0; c < classIndexMap.size(); c++) { // barcha class
          for (int t = 0; t < teacherIndexMap.size(); t++) { // barcha teacher
            for (int s = 0; s < subjectIndexMap.size(); s++) { // barcha subject
              if (timetable[c][r][t][h][s] != null) {
                roomLessonsAtHour.add(timetable[c][r][t][h][s]);
              }
            }
          }
        }
        // Shu room uchun shu soatda faqat 1ta dars bo'lishi mumkin
        model.addAtMostOne(roomLessonsAtHour.toArray(new BoolVar[0]));
      }
    }
  }

  private void addConstraintsToTeacher(
      CpModel model,
      BoolVar[][][][][] timetable,
      HashMap<Integer, Integer> classIndexMap,
      HashMap<Integer, Integer> teacherIndexMap,
      HashMap<Integer, Integer> subjectIndexMap,
      int roomCount,
      int hourCount) {

    for (int t = 0; t < teacherIndexMap.size(); t++) { // har bir teacher
      for (int h = 0; h < hourCount; h++) { // har bir hour
        List<BoolVar> teacherLessonsAtHour = new ArrayList<>();
        for (int c = 0; c < classIndexMap.size(); c++) { // barcha class
          for (int r = 0; r < roomCount; r++) { // barcha room
            for (int s = 0; s < subjectIndexMap.size(); s++) { // barcha subject
              if (timetable[c][r][t][h][s] != null) {
                teacherLessonsAtHour.add(timetable[c][r][t][h][s]);
              }
            }
          }
        }
        // Shu teacher uchun, shu hour da faqat 1ta dars bo'lishi kerak
        model.addAtMostOne(teacherLessonsAtHour.toArray(new BoolVar[0]));
      }
    }
  }

  private List<Response> solveAndGenerateTimetable(
      CpModel model,
      Integer hourCount,
      Map<Integer, Integer> subjectIndexMap,
      Map<Integer, Integer> teacherIndexMap,
      Map<Integer, Integer> classIndexMap,
      BoolVar[][][][][] timetable,
      final int roomCount) {

    System.out.println("Start Solving: " + LocalDateTime.now());
    CpSolver solver = new CpSolver();
    CpSolverStatus status = solver.solve(model);

    //    if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
    if (status == CpSolverStatus.OPTIMAL) {
      System.out.println("Solution found:");
      List<Response> timetables = new ArrayList<>();

      for (int c = 0; c < classIndexMap.size(); c++) {
        int classId = classIndexMap.get(c);

        for (int r = 0; r < roomCount; r++) {
          for (int t = 0; t < teacherIndexMap.size(); t++) {
            int teacherId = teacherIndexMap.get(t);

            for (int h = 0; h < hourCount; h++) {
              for (int s = 0; s < subjectIndexMap.size(); s++) {
                int subjectId = subjectIndexMap.get(s);

                if (timetable[c][r][t][h][s] != null
                    && solver.booleanValue(timetable[c][r][t][h][s])) {

                  Response response = new Response();
                  response.setClassObj(DemoData.classMap().get(classId));
                  response.setRoomId(r); // agar Room id map bo‘lsa shu joyda qo‘shasiz
                  response.setTeacher(DemoData.getTeacherMap().get(teacherId));
                  response.setDay(getDayOfWeek(h + 1));
                  response.setHour(getHourOfDay(h + 1));
                  response.setSubject(DemoData.getSubjectMap().get(subjectId));
                  timetables.add(response);
                  System.out.printf(
                      "Assigned: Class %s, Room %d, Teacher %s, Hour %d, Subject %s%n",
                      response.getClassObj().getName(),
                      r,
                      response.getTeacher().getName(),
                      h,
                      response.getSubject().getName());
                }
              }
            }
          }
        }
      }

      System.out.println("\nStatistics");
      System.out.println("  - conflicts      : " + solver.numConflicts());
      System.out.println("  - branches       : " + solver.numBranches());
      System.out.println("  - wall time      : " + solver.wallTime() + " s");

      return timetables;
    }

    System.out.println("No solution found.");
    return Collections.emptyList();
  }

  private void addSoftConstraintsForContinuity(
      CpModel model,
      BoolVar[][][][][] timetable,
      HashMap<Integer, Integer> classIndexMap,
      HashMap<Integer, Integer> teacherIndexMap,
      int roomCount,
      int hoursCount,
      int subjectsCount,
      LinearExprBuilder globalObjective) {

    // Umumiy jarimalar (penalties) yig'indisini saqlash uchun o'zgaruvchi

    int dailyHours = 7; // Bir kunda nechta soat dars borligi

    // =================================================================
    // 1. SINFLAR UCHUN OCHIQ SOATLARNI MINIMALLASHTIRISH (YUQORI JARIMA)
    // =================================================================
    final int classGapPenalty = 3; // Sinf uchun ochiq soat jarimasi

    for (int c = 0; c < classIndexMap.size(); c++) {
      // Har bir kun uchun alohida tekshiramiz
      for (int day = 0; day < (hoursCount / dailyHours); day++) {
        int startHour = day * dailyHours;
        for (int h = startHour; h < startHour + dailyHours - 2; h++) {

          // h-soatda dars borligini aniqlaydigan o'zgaruvchi
          BoolVar lessonAtH = model.newBoolVar("class_" + c + "_has_lesson_at_" + h);
          List<BoolVar> lessonsAtHList = new ArrayList<>();
          for (int r = 0; r < roomCount; r++)
            for (int t = 0; t < teacherIndexMap.size(); t++)
              for (int s = 0; s < subjectsCount; s++) {
                if (timetable[c][r][t][h][s] != null) lessonsAtHList.add(timetable[c][r][t][h][s]);
              }
          // lessonAtH <=> (darslar ro'yxatidan kamida bittasi true)
          model.addBoolOr(lessonsAtHList.toArray(new BoolVar[0])).onlyEnforceIf(lessonAtH);
          model
              .addBoolAnd(lessonsAtHList.stream().map(BoolVar::not).toArray(Literal[]::new))
              .onlyEnforceIf(lessonAtH.not());

          // (h+1)-soatda dars YO'QLIGINI aniqlaydigan o'zgaruvchi
          BoolVar noLessonAtH1 = model.newBoolVar("class_" + c + "_no_lesson_at_" + (h + 1));
          List<BoolVar> lessonsAtH1List = new ArrayList<>();
          for (int r = 0; r < roomCount; r++)
            for (int t = 0; t < teacherIndexMap.size(); t++)
              for (int s = 0; s < subjectsCount; s++) {
                if (timetable[c][r][t][h + 1][s] != null)
                  lessonsAtH1List.add(timetable[c][r][t][h + 1][s]);
              }
          // noLessonAtH1 <=> (darslar ro'yxatining hammasi false)
          model
              .addBoolAnd(lessonsAtH1List.stream().map(BoolVar::not).toArray(Literal[]::new))
              .onlyEnforceIf(noLessonAtH1);
          model
              .addBoolOr(lessonsAtH1List.toArray(new BoolVar[0]))
              .onlyEnforceIf(noLessonAtH1.not());

          // (h+2)-soatda dars borligini aniqlaydigan o'zgaruvchi
          BoolVar lessonAtH2 = model.newBoolVar("class_" + c + "_has_lesson_at_" + (h + 2));
          List<BoolVar> lessonsAtH2List = new ArrayList<>();
          for (int r = 0; r < roomCount; r++)
            for (int t = 0; t < teacherIndexMap.size(); t++)
              for (int s = 0; s < subjectsCount; s++) {
                if (timetable[c][r][t][h + 2][s] != null)
                  lessonsAtH2List.add(timetable[c][r][t][h + 2][s]);
              }
          // lessonAtH2 <=> (darslar ro'yxatidan kamida bittasi true)
          model.addBoolOr(lessonsAtH2List.toArray(new BoolVar[0])).onlyEnforceIf(lessonAtH2);
          model
              .addBoolAnd(lessonsAtH2List.stream().map(BoolVar::not).toArray(Literal[]::new))
              .onlyEnforceIf(lessonAtH2.not());

          // Ochiq soat (gap) mavjudligini bildiruvchi o'zgaruvchi
          // hasGap <=> (lessonAtH AND noLessonAtH1 AND lessonAtH2)
          BoolVar hasGap = model.newBoolVar("class_" + c + "_has_gap_at_" + h);
          model
              .addBoolAnd(new BoolVar[] {lessonAtH, noLessonAtH1, lessonAtH2})
              .onlyEnforceIf(hasGap);
          model
              .addBoolOr(new Literal[] {lessonAtH.not(), noLessonAtH1.not(), lessonAtH2.not()})
              .onlyEnforceIf(hasGap.not());

          // Agar ochiq soat bo'lsa, umumiy jarimaga qo'shamiz
          globalObjective.addTerm(hasGap, classGapPenalty);
        }
      }
    }

    // ======================================================================
    // 2. O'QITUVCHILAR UCHUN OCHIQ SOATLARNI MINIMALLASHTIRISH (PAST JARIMA)
    // ======================================================================
    final int teacherGapPenalty = 1; // O'qituvchi uchun ochiq soat jarimasi

    for (int t = 0; t < teacherIndexMap.size(); t++) {
      for (int day = 0; day < (hoursCount / dailyHours); day++) {
        int startHour = day * dailyHours;
        for (int h = startHour; h < startHour + dailyHours - 2; h++) {

          BoolVar lessonAtH = model.newBoolVar("teacher_" + t + "_has_lesson_at_" + h);
          List<BoolVar> lessonsAtHList = new ArrayList<>();
          for (int c = 0; c < classIndexMap.size(); c++)
            for (int r = 0; r < roomCount; r++)
              for (int s = 0; s < subjectsCount; s++) {
                if (timetable[c][r][t][h][s] != null) lessonsAtHList.add(timetable[c][r][t][h][s]);
              }
          model.addBoolOr(lessonsAtHList.toArray(new BoolVar[0])).onlyEnforceIf(lessonAtH);
          model
              .addBoolAnd(lessonsAtHList.stream().map(BoolVar::not).toArray(Literal[]::new))
              .onlyEnforceIf(lessonAtH.not());

          BoolVar noLessonAtH1 = model.newBoolVar("teacher_" + t + "_no_lesson_at_" + (h + 1));
          List<BoolVar> lessonsAtH1List = new ArrayList<>();
          for (int c = 0; c < classIndexMap.size(); c++)
            for (int r = 0; r < roomCount; r++)
              for (int s = 0; s < subjectsCount; s++) {
                if (timetable[c][r][t][h + 1][s] != null)
                  lessonsAtH1List.add(timetable[c][r][t][h + 1][s]);
              }
          model
              .addBoolAnd(lessonsAtH1List.stream().map(BoolVar::not).toArray(Literal[]::new))
              .onlyEnforceIf(noLessonAtH1);
          model
              .addBoolOr(lessonsAtH1List.toArray(new BoolVar[0]))
              .onlyEnforceIf(noLessonAtH1.not());

          BoolVar lessonAtH2 = model.newBoolVar("teacher_" + t + "_has_lesson_at_" + (h + 2));
          List<BoolVar> lessonsAtH2List = new ArrayList<>();
          for (int c = 0; c < classIndexMap.size(); c++)
            for (int r = 0; r < roomCount; r++)
              for (int s = 0; s < subjectsCount; s++) {
                if (timetable[c][r][t][h + 2][s] != null)
                  lessonsAtH2List.add(timetable[c][r][t][h + 2][s]);
              }
          model.addBoolOr(lessonsAtH2List.toArray(new BoolVar[0])).onlyEnforceIf(lessonAtH2);
          model
              .addBoolAnd(lessonsAtH2List.stream().map(BoolVar::not).toArray(Literal[]::new))
              .onlyEnforceIf(lessonAtH2.not());

          BoolVar hasGap = model.newBoolVar("teacher_" + t + "_has_gap_at_" + h);
          model
              .addBoolAnd(new BoolVar[] {lessonAtH, noLessonAtH1, lessonAtH2})
              .onlyEnforceIf(hasGap);
          model
              .addBoolOr(new Literal[] {lessonAtH.not(), noLessonAtH1.not(), lessonAtH2.not()})
              .onlyEnforceIf(hasGap.not());

          globalObjective.addTerm(hasGap, teacherGapPenalty);
        }
      }
    }
  }

  private static DayOfWeek getDayOfWeek(int hour) {
    int dayIndex = (hour - 1) / 7; // 0-based
    return DayOfWeek.of(dayIndex + 1); // MONDAY=1
  }

  private static int getHourOfDay(int hour) {
    return ((hour - 1) % 7) + 1; // 1–7
  }
}
