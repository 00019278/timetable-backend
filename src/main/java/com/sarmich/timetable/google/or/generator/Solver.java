package com.sarmich.timetable.google.or.generator;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import com.sarmich.timetable.google.or.models.Response;
import com.sarmich.timetable.google.or.models.Lesson;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class Solver {
  public List<Response> generate(List<Lesson> lessons) {
    log.info("Start : {}", LocalDateTime.now());
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
        createTimetableVariablesWithClass(
            model, hoursCount, classIndexMap, roomCount, teacherIdMap, subjectIdMap, lessons);

    // Add constraints
    addConstraints(
        model, timetable, classIndexMap, teacherIndexMap, subjectIndexMap, roomCount, hoursCount);

    return solveAndGenerateTimetable(
        model,
        lessons,
        hoursCount,
        subjectIndexMap,
        teacherIndexMap,
        classIndexMap,
        timetable,
        roomCount);
  }

  private BoolVar[][][][][] createTimetableVariablesWithClass(
      CpModel model,
      Integer hourCount,
      HashMap<Integer, Integer> classIndexMap,
      int roomCount,
      HashMap<Integer, Integer> teacherMap,
      HashMap<Integer, Integer> subjectMap,
      List<Lesson> lessons) {
    //      Class c uchun, Room rda, Teacher t bilan, Hour h da, Subject s qo‘yildimi yoki yo‘qmi?
    BoolVar[][][][][] timetable =
        new BoolVar[classIndexMap.size()][roomCount][teacherMap.size()][hourCount]
            [subjectMap.size()];

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
    for (int r = 0; r < roomCount; r++) {
      for (int h = 0; h < hourCount; h++) {
        for (int c = 0; c < classIndexMap.size(); c++) {
          List<BoolVar> classLessonsAtHour = new ArrayList<>();
          for (int t = 0; t < teacherIndexMap.size(); t++) {
            for (int s = 0; s < subjectIndexMap.size(); s++) {
              if (timetable[c][r][t][h][s] != null) {
                classLessonsAtHour.add(timetable[c][r][t][h][s]);
              }
            }
          }
          model.addAtMostOne(classLessonsAtHour.toArray(new BoolVar[0]));
        }

        for (int t = 0; t < teacherIndexMap.size(); t++) {
          List<BoolVar> teacherLessonsAtHour = new ArrayList<>();
          for (int c = 0; c < classIndexMap.size(); c++) {
            for (int s = 0; s < subjectIndexMap.size(); s++) {
              if (timetable[c][r][t][h][s] != null) {
                teacherLessonsAtHour.add(timetable[c][r][t][h][s]);
              }
            }
          }
          model.addAtMostOne(teacherLessonsAtHour.toArray(new BoolVar[0]));
        }

        for (int c = 0; c < classIndexMap.size(); c++) {
          for (int t = 0; t < teacherIndexMap.size(); t++) {
            List<BoolVar> roomLessonsAtHour = new ArrayList<>();
            for (int s = 0; s < subjectIndexMap.size(); s++) {
              if (timetable[c][r][t][h][s] != null) {
                roomLessonsAtHour.add(timetable[c][r][t][h][s]);
              }
            }
            model.addAtMostOne(roomLessonsAtHour.toArray(new BoolVar[0]));
          }
        }
      }
    }
  }

  private List<Response> solveAndGenerateTimetable(
      CpModel model,
      List<Lesson> lessons,
      Integer hourCount,
      Map<Integer, Integer> subjectIndexMap,
      Map<Integer, Integer> teacherIndexMap,
      Map<Integer, Integer> classIndexMap,
      BoolVar[][][][][] timetable,
      final int roomCount) {
    System.out.println("Start Solving: " + LocalDateTime.now());
    CpSolver solver = new CpSolver();
    CpSolverStatus status = solver.solve(model);

    if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
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
                if (timetable[c][r][t][h][s] != null) {
                  if (solver.booleanValue(timetable[c][r][t][h][s])) {
                    Response response = new Response();
                    //                                        response.setClassId(classId);
                    //                                        response.setRoom(r);
                    //                                        response.setTeacherId(teacherId);
                    //                                        response.setHour(h);
                    //                                        response.setSubjectId(subjectId);
                    timetables.add(response);

                    System.out.printf(
                        "Assigned: Class %d, Room %d, Teacher %d, Hour %d, Subject %d%n",
                        classId, r, teacherId, h, subjectId);
                  }
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
    return null;
  }
}
