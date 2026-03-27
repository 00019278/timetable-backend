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
public class SolverClone {
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
    Integer hoursCount = 42;

    BoolVar[][][][][] timetable =
        createTimetableVariablesWithClassClone(
            model, hoursCount, classIndexMap, roomCount, teacherIdMap, subjectIdMap, lessons);
    // Add constraints
    addConstraints(
        model, timetable, classIndexMap, teacherIndexMap, subjectIndexMap, roomCount, hoursCount);

    for (Lesson lesson : lessons) {
      int classIdx = classIdMap.get(lesson.getClassId());
      int teacherIdx = teacherIdMap.get(lesson.getTeacherId());
      int subjectIdx = subjectIdMap.get(lesson.getSubjectId());

      List<BoolVar> possibleSlots = new ArrayList<>();

      for (int r = 0; r < roomCount; r++) {
        for (int h = 0; h < hoursCount; h++) {
          BoolVar var = timetable[classIdx][r][teacherIdx][h][subjectIdx];
          if (var != null) {
            possibleSlots.add(var);
          }
        }
      }
      // Lessonni kamida 1 joyga qo‘yish kerak
      model.addExactlyOne(possibleSlots.toArray(new BoolVar[0]));
    }

    return solveAndGenerateTimetable(
        model, hoursCount, subjectIndexMap, teacherIndexMap, classIndexMap, timetable, roomCount);
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

  private static DayOfWeek getDayOfWeek(int hour) {
    int dayIndex = (hour - 1) / 7; // 0-based
    return DayOfWeek.of(dayIndex + 1); // MONDAY=1
  }

  private static int getHourOfDay(int hour) {
    return ((hour - 1) % 7) + 1; // 1–7
  }
}
