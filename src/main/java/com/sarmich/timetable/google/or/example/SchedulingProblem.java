//package com.sarmich.timetable.google.or;
//
//import com.google.ortools.Loader;
//import com.google.ortools.sat.*;
//import com.sarmich.timetable.google.or.generate.Class;
//import com.sarmich.timetable.google.or.models.DemoData;
//import com.sarmich.timetable.google.or.models.Response;
//
//import java.time.DayOfWeek;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import com.sarmich.timetable.google.or.models.Lesson;
//import com.sarmich.timetable.google.or.models.Subject;
//import com.sarmich.timetable.google.or.models.Teacher;
//
//public class SchedulingProblem {
//
//    public List<Response> generate(List<Lesson> lessons) {
//        System.out.println("Start : " + LocalDateTime.now());
//
//        Map<Integer, Integer> courseTeacherMap = new HashMap<>();
//        List<Integer> courses = lessons.stream()
//                .map(i -> {
//                    int cId = combineIds(i.getSubjectId(), i.getClassId());
//                    courseTeacherMap.put(cId, i.getTeacherId());
//                    return cId;
//                })
//                .toList();
//
//        List<Integer> teachers = lessons.stream()
//                .map(Lesson::getTeacherId)
//                .collect(Collectors.toSet())
//                .stream()
//                .toList();
//
//        Loader.loadNativeLibraries();
//        CpModel model = new CpModel();
//
//        int numTeachers = teachers.size();
//        int numRooms = 50;
//        int numCourses = courses.size();
//        List<Integer> numHoursList = List.of(
//                1000, 1001, 1002, 1003, 1004, 1005,
//                2000, 2001, 2002, 2003, 2004, 2005,
//                3000, 3001, 3002, 3003, 3004, 3005,
//                4000, 4001, 4002, 4003, 4004, 4005,
//                5000, 5001, 5002, 5003, 5004, 5005,
//                6000, 6001, 6002, 6003, 6004, 6005
//        );
//
//        BoolVar[][][][] timetable = createTimetableVariables(model, numHoursList, numCourses, numRooms, numTeachers, courses, courseTeacherMap, teachers);
//
//        addTeacherConstraints(model, numHoursList, numTeachers, numRooms, lessons, courses, teachers, courseTeacherMap, timetable);
////        addRoomConstraints(model, numHoursList, numRooms, numTeachers, lessons, courses, teachers, courseTeacherMap, timetable);
////        addCourseConstraints(model, numHoursList, numCourses, numRooms, courseTeacherMap, teachers, timetable, courses);
////        addClassConstraints(model, numHoursList, numCourses, numRooms, courses, courseTeacherMap, teachers, timetable);
////        addWeeklyCourseConstraints(model, lessons, numHoursList, courses, courseTeacherMap, teachers, timetable);
////        addNoBreakConstraints(model, numHoursList, numCourses, numRooms, courses, courseTeacherMap, teachers, timetable);
//
//        return solveAndGenerateTimetable(model, lessons, numHoursList, courses, courseTeacherMap, teachers, timetable);
//    }
//
//    private BoolVar[][][][] createTimetableVariables(CpModel model, List<Integer> numHoursList, int numCourses, int numRooms, int numTeachers, List<Integer> courses, Map<Integer, Integer> courseTeacherMap, List<Integer> teachers) {
//        BoolVar[][][][] timetable = new BoolVar[numHoursList.size()][numCourses][numRooms][numTeachers];
//        for (int h = 0; h < numHoursList.size(); h++) {
//            for (int c = 0; c < numCourses; c++) {
//                for (int r = 0; r < numRooms; r++) {
//                    int courseId = courses.get(c);
//                    int teacherId = courseTeacherMap.get(courseId);
//                    teacherId = teachers.indexOf(teacherId);
//                    timetable[h][c][r][teacherId] = model.newBoolVar("h" + h + "_c" + c + "_r" + r + "_t" + teacherId);
//                }
//            }
//        }
//        return timetable;
//    }
//
//    private void addTeacherConstraints(CpModel model, List<Integer> numHoursList, int numTeachers, int numRooms, List<Lesson> lessons, List<Integer> courses, List<Integer> teachers, Map<Integer, Integer> courseTeacherMap, BoolVar[][][][] timetable) {
//        /** timetable[][][][]; o'zgaruvchi bor
//         1-oqituvchi uchun       timetable[0]
//         1-soat uchun           timetable[0][0]
//         n ta xona uchun
//         timetable[0][0][0] ->
//         timetable[0][0][1] ->
//         timetable[0][0][2] ->
//         timetable[0][0][3] ->
//         shulardan faqat bittasi true qiymatni qabul qilishi kerak shunda oqituvchi bitta xonada dars beraid
//         nima uchun course oxirida sababi coursega bitta xonada bitta oqituvchi dars oqtishining umuman aloqasi yo'q
//
//         */
//        for (int t = 0; t < numTeachers; t++) {
//            for (int h = 0; h < numHoursList.size(); h++) {
//                ArrayList<Literal> boolVars = new ArrayList<>();
//                for (int r = 0; r < numRooms; r++) {
//                    int finalT = teachers.get(t);
//                    List<Integer> teacherCourses = lessons.stream()
//                            .filter(l -> l.getTeacherId() == finalT)
//                            .map(l -> combineIds(l.getSubjectId(), l.getClassId()))
//                            .toList();
//                    for (Integer teacherCourse : teacherCourses) {
//                        int index = courses.indexOf(teacherCourse);
//                        boolVars.add(timetable[h][index][r][t]);
//                    }
//                }
//                model.addAtMostOne(boolVars.toArray(new Literal[0]));
//            }
//        }
//    }
//
//    private void addRoomConstraints(CpModel model, List<Integer> numHoursList, int numRooms, int numTeachers, List<Lesson> lessons, List<Integer> courses, List<Integer> teachers, Map<Integer, Integer> courseTeacherMap, BoolVar[][][][] timetable) {
//        for (int r = 0; r < numRooms; r++) {
//            for (int h = 0; h < numHoursList.size(); h++) {
//                ArrayList<Literal> boolVars = new ArrayList<>();
//                for (int t = 0; t < numTeachers; t++) {
//                    int finalT = teachers.get(t);
//                    List<Integer> teacherCourses = lessons.stream()
//                            .filter(l -> l.getTeacherId() == finalT)
//                            .map(l -> combineIds(l.getSubjectId(), l.getClassId()))
//                            .toList();
//                    for (Integer teacherCourse : teacherCourses) {
//                        int index = courses.indexOf(teacherCourse);
//                        boolVars.add(timetable[h][index][r][t]);
//                    }
//                }
//                model.addAtMostOne(boolVars.toArray(new Literal[0]));
//            }
//        }
//    }
//
//    private void addCourseConstraints(CpModel model, List<Integer> numHoursList, int numCourses, int numRooms, Map<Integer, Integer> courseTeacherMap, List<Integer> teachers, BoolVar[][][][] timetable, final List<Integer> courses) {
//        for (int c = 0; c < numCourses; c++) {
//            for (int h = 0; h < numHoursList.size(); h++) {
//                ArrayList<Literal> boolVars = new ArrayList<>();
//                int courseId = courses.get(c);
//                int teacherId = courseTeacherMap.get(courseId);
//                teacherId = teachers.indexOf(teacherId);
//                for (int r = 0; r < numRooms; r++) {
//                    boolVars.add(timetable[h][c][r][teacherId]);
//                }
//                model.addAtMostOne(boolVars.toArray(new Literal[0]));
//            }
//        }
//    }
//
//    private void addClassConstraints(CpModel model, List<Integer> numHoursList, int numCourses, int numRooms, List<Integer> courses, Map<Integer, Integer> courseTeacherMap, List<Integer> teachers, BoolVar[][][][] timetable) {
//        for (int h = 0; h < numHoursList.size(); h++) {
//            Map<Integer, ArrayList<Literal>> classBoolVarsMap = new HashMap<>();
//            for (int c = 0; c < numCourses; c++) {
//                int classId = extractClassId(courses.get(c));
//                classBoolVarsMap.putIfAbsent(classId, new ArrayList<>());
//                int teacherId = courseTeacherMap.get(courses.get(c));
//                teacherId = teachers.indexOf(teacherId);
//                for (int r = 0; r < numRooms; r++) {
//                    classBoolVarsMap.get(classId).add(timetable[h][c][r][teacherId]);
//                }
//            }
//            for (ArrayList<Literal> boolVars : classBoolVarsMap.values()) {
//                model.addAtMostOne(boolVars.toArray(new Literal[0]));
//            }
//        }
//    }
//
//    private void addWeeklyCourseConstraints(CpModel model, List<Lesson> lessons, List<Integer> numHoursList, List<Integer> courses, Map<Integer, Integer> courseTeacherMap, List<Integer> teachers, BoolVar[][][][] timetable) {
//        for (int c = 0; c < courses.size(); c++) {
//            int courseId = courses.get(c);
//            int teacherId = courseTeacherMap.get(courseId);
//            teacherId = teachers.indexOf(teacherId);
//            int requiredCount = lessons.stream()
//                    .filter(l -> combineIds(l.getSubjectId(), l.getClassId()) == courseId)
//                    .findFirst()
//                    .get()
//                    .getCount();
//            ArrayList<Literal> boolVars = new ArrayList<>();
//            for (int h = 0; h < numHoursList.size(); h++) {
//                for (int r = 0; r < 50; r++) {
//                    boolVars.add(timetable[h][c][r][teacherId]);
//                }
//            }
//            model.addEquality(LinearExpr.sum(boolVars.toArray(new Literal[0])), requiredCount);
//        }
//    }
//
//    private void addNoBreakConstraints(CpModel model, List<Integer> numHoursList, int numCourses, int numRooms, List<Integer> courses, Map<Integer, Integer> courseTeacherMap, List<Integer> teachers, BoolVar[][][][] timetable) {
//        int daysInWeek = 6;
//        int hoursPerDay = numHoursList.size() / daysInWeek;
//
//        for (int c = 0; c < numCourses; c++) {
//            for (int d = 0; d < daysInWeek; d++) {
//                for (int h = 1; h < hoursPerDay; h++) {
//                    int courseId = courses.get(c);
//                    int teacherId = courseTeacherMap.get(courseId);
//                    teacherId = teachers.indexOf(teacherId);
//
//                    BoolVar hasLessonInPrevHour = model.newBoolVar("hasLessonInPrevHour_" + d + "_" + h + "_c" + c);
//
//                    ArrayList<Literal> precedingVars = new ArrayList<>();
//                    for (int r = 0; r < numRooms; r++) {
//                        precedingVars.add(timetable[d * hoursPerDay + h - 1][c][r][teacherId]);
//                    }
//
//                    model.addGreaterOrEqual(LinearExpr.sum(precedingVars.toArray(new Literal[0])), 1).onlyEnforceIf(hasLessonInPrevHour);
//
//                    for (int r = 0; r < numRooms; r++) {
//                        BoolVar currentVar = timetable[d * hoursPerDay + h][c][r][teacherId];
//                        model.addImplication(currentVar, hasLessonInPrevHour);
//                    }
//                }
//            }
//        }
//    }
//
//    private List<Response> solveAndGenerateTimetable(CpModel model, List<Lesson> lessons, List<Integer> numHoursList, List<Integer> courses, Map<Integer, Integer> courseTeacherMap, List<Integer> teachers, BoolVar[][][][] timetable) {
//        System.out.println("Start Solving: " + LocalDateTime.now());
//        CpSolver solver = new CpSolver();
//        CpSolverStatus status = solver.solve(model);
//        Map<Integer, Teacher> teacherMap = DemoData.getTeacherMap();
//        Map<Integer, Subject> subjectMap = DemoData.getSubjectMap();
//        Map<Integer, Class> classMap = DemoData.getClassMap();
//
//        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
//            System.out.println("Solution found:");
//            List<Response> timetables = new ArrayList<>();
//
//            for (int h = 0; h < numHoursList.size(); h++) {
//                for (int c = 0; c < courses.size(); c++) {
//                    int courseId = courses.get(c);
//                    int teacherId = courseTeacherMap.get(courseId);
//                    teacherId = teachers.indexOf(teacherId);
//                    for (int r = 0; r < timetable[0][0].length; r++) {
//                        if (timetable[h][c][r] != null && solver.booleanValue(timetable[h][c][r][teacherId])) {
//                            timetables.add(new Response(
//                                    DayOfWeek.of(numHoursList.get(h) / 1000),
//                                    numHoursList.get(h) % 1000,
//                                    teacherMap.get(teachers.get(teacherId)),
//                                    subjectMap.get(extractSubjectId(courses.get(c))),
//                                    classMap.get(extractClassId(courses.get(c)))
//                            ));
//                            System.out.println("Hour: " + numHoursList.get(h) +
//                                    ", course: " + courses.get(c) +
//                                    ", Class: " + extractClassId(courses.get(c)) +
//                                    ", Subject: " + subjectMap.get(extractSubjectId(courses.get(c))).getName() +
//                                    ", Room: " + r +
//                                    ", Teacher: " + teacherMap.get(teachers.get(teacherId)).getName());
//                        }
//                    }
//                }
//            }
//
//            System.out.println("\nStatistics");
//            System.out.println("  - conflicts      : " + solver.numConflicts());
//            System.out.println("  - branches       : " + solver.numBranches());
//            System.out.println("  - wall time      : " + solver.wallTime() + " s");
//            return timetables;
//        }
//
//        return null;
//    }
//
//    private int combineIds(int subjectId, int classId) {
//        return subjectId * 10000 + classId;
//    }
//
//    private int extractClassId(int combinedId) {
//        return combinedId % 10000;
//    }
//
//    private int extractSubjectId(int combinedId) {
//        return combinedId / 10000;
//    }
//}
