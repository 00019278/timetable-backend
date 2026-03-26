package com.sarmich.timetable.google.or.generate;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import com.sarmich.timetable.google.or.models.Lesson;
import com.sarmich.timetable.google.or.models.Subject;
import com.sarmich.timetable.google.or.models.Teacher;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SchedulingProblem {
    public static void generate(List<Lesson> lessons) {
        System.out.println("Start : " + LocalDateTime.now());

        HashMap<Integer, Integer> map = new HashMap<>();
        List<Integer> courses = lessons.stream()
                .map(i -> {
                    int cId = combineIds(i.getSubjectId(), i.getClassId());
                    map.put(cId, i.getTeacherId());
                    return cId;
                })
                .toList();

        List<Integer> teachers = lessons.stream()
                .map(Lesson::getTeacherId)
                .collect(Collectors.toSet())
                .stream()
                .toList();

        Loader.loadNativeLibraries();
        CpModel model = new CpModel();

        int numTeachers = teachers.size();
        int numRooms = 50;
        int numCourses = courses.size();
        List<Integer> numHoursList = List.of(
                1000, 1001, 1002, 1003, 1004, 1005,
                2000, 2001, 2002, 2003, 2004, 2005,
                3000, 3001, 3002, 3003, 3004, 3005,
                4000, 4001, 4002, 4003, 4004, 4005,
                5000, 5001, 5002, 5003, 5004, 5005,
                6000, 6001, 6002, 6003, 6004, 6005
        );

        BoolVar[][][][] timetable = new BoolVar[numHoursList.size()][numCourses][numRooms][numTeachers];
        for (int h = 0; h < numHoursList.size(); h++) {
            for (int c = 0; c < numCourses; c++) {
                for (int r = 0; r < numRooms; r++) {
                    int courseId = courses.get(c);
                    int teacherId = map.get(courseId);
                    for (int i = 0; i < teachers.size(); i++) {
                        if (teachers.get(i) == teacherId) {
                            teacherId = i;
                            break;
                        }
                    }
                    timetable[h][c][r][teacherId] = model.newBoolVar("h" + h + "_c" + c + "_r" + r + "_t" + teacherId);
                }
            }
        }

        // Teacher constraints: A teacher can only be in one room at one hour
        for (int t = 0; t < numTeachers; t++) {
            for (int h = 0; h < numHoursList.size(); h++) {
                ArrayList<Literal> boolVars = new ArrayList<>();
                for (int r = 0; r < numRooms; r++) {
                    int finalT = teachers.get(t);
                    List<Integer> teacherCourses = lessons.stream().filter(l -> l.getTeacherId() == finalT).map(l -> combineIds(l.getSubjectId(), l.getClassId())).toList();
                    for (Integer teacherCourse : teacherCourses) {
                        int index = -1;
                        for (int i = 0; i < courses.size(); i++) {
                            if (Objects.equals(courses.get(i), teacherCourse)) {
                                index = i;
                                break;
                            }
                        }
                        boolVars.add(timetable[h][index][r][t]);
                    }
                }
                model.addAtMostOne(boolVars.toArray(new Literal[0]));
            }
        }

        // Room constraints: A room can only be used by one teacher at one hour
        for (int r = 0; r < numRooms; r++) {
            for (int h = 0; h < numHoursList.size(); h++) {
                ArrayList<Literal> boolVars = new ArrayList<>();
                for (int t = 0; t < numTeachers; t++) {
                    int finalT = teachers.get(t);
                    List<Integer> teacherCourses = lessons.stream().filter(l -> l.getTeacherId() == finalT).map(l -> combineIds(l.getSubjectId(), l.getClassId())).toList();
                    for (Integer teacherCourse : teacherCourses) {
                        int index = -1;
                        for (int i = 0; i < courses.size(); i++) {
                            if (Objects.equals(courses.get(i), teacherCourse)) {
                                index = i;
                                break;
                            }
                        }
                        boolVars.add(timetable[h][index][r][t]);
                    }
                }
                model.addAtMostOne(boolVars.toArray(new Literal[0]));
            }
        }

        // Class constraints: A class can only be in one room at one hour
        for (int c = 0; c < numCourses; c++) {
            for (int h = 0; h < numHoursList.size(); h++) {
                ArrayList<Literal> boolVars = new ArrayList<>();
                int courseId = courses.get(c);
                int teacherId = map.get(courseId);
                for (int i = 0; i < teachers.size(); i++) {
                    if (teachers.get(i) == teacherId) {
                        teacherId = i;
                        break;
                    }
                }
                for (int r = 0; r < numRooms; r++) {
                    boolVars.add(timetable[h][c][r][teacherId]);
                }
                model.addAtMostOne(boolVars.toArray(new Literal[0]));
            }
        }

        for (int h = 0; h < numHoursList.size(); h++) {
            Map<Integer, ArrayList<Literal>> classBoolVarsMap = new HashMap<>();
            for (int c = 0; c < numCourses; c++) {
                int classId = extractClassId(courses.get(c));
                classBoolVarsMap.putIfAbsent(classId, new ArrayList<>());
                for (int r = 0; r < numRooms; r++) {
                    int teacherId = map.get(courses.get(c));
                    for (int i = 0; i < teachers.size(); i++) {
                        if (teachers.get(i) == teacherId) {
                            teacherId = i;
                            break;
                        }
                    }
                    classBoolVarsMap.get(classId).add(timetable[h][c][r][teacherId]);
                }
            }
            for (ArrayList<Literal> boolVars : classBoolVarsMap.values()) {
                model.addAtMostOne(boolVars.toArray(new Literal[0]));
            }
        }
        // Priority constraints: List to hold violation indicators
        List<BoolVar> highPriorityViolations = new ArrayList<>();
        List<BoolVar> middlePriorityViolations = new ArrayList<>();

        // High priority constraint: Consecutive classes for each course
        for (int c = 0; c < numCourses; c++) {
            int courseId = courses.get(c);
            int teacherId = map.get(courseId);
            for (int i = 0; i < teachers.size(); i++) {
                if (teachers.get(i) == teacherId) {
                    teacherId = i;
                    break;
                }
            }
            int requiredCount = lessons.stream().filter(l -> combineIds(l.getSubjectId(), l.getClassId()) == courseId).findFirst().get().getCount();
            for (int r = 0; r < numRooms; r++) {
                for (int startHour = 0; startHour < numHoursList.size() - requiredCount + 1; startHour++) {
                    BoolVar consecutiveViolation = model.newBoolVar("consecutiveViolation_" + c + "_" + startHour);
                    ArrayList<Literal> consecutiveVars = new ArrayList<>();
                    for (int h = startHour; h < startHour + requiredCount; h++) {
                        consecutiveVars.add(timetable[h][c][r][teacherId]);
                    }
                    // Ensure that if the class starts at startHour, it continues for requiredCount consecutive hours
                    for (int h = startHour; h < startHour + requiredCount - 1; h++) {
                        model.addImplication(timetable[startHour][c][r][teacherId], timetable[h + 1][c][r][teacherId]);
                    }
                    // Add the violation variable
                    model.addBoolOr(consecutiveVars.toArray(new Literal[0])).onlyEnforceIf(consecutiveViolation.not());
                    highPriorityViolations.add(consecutiveViolation);
                }
            }
        }

        // Middle priority constraint: Consecutive classes for each teacher
        for (int t = 0; t < numTeachers; t++) {
            int finalT = teachers.get(t);
            List<Integer> teacherCourses = lessons.stream().filter(l -> l.getTeacherId() == finalT).map(l -> combineIds(l.getSubjectId(), l.getClassId())).toList();
            for (int r = 0; r < numRooms; r++) {
                for (int h = 0; h < numHoursList.size(); h++) {
                    for (Integer teacherCourse : teacherCourses) {
                        int index = -1;
                        for (int i = 0; i < courses.size(); i++) {
                            if (Objects.equals(courses.get(i), teacherCourse)) {
                                index = i;
                                break;
                            }
                        }
                        BoolVar middleViolation = model.newBoolVar("middleViolation_" + t + "_" + h + "_" + r);
                        // Impose consecutive constraint for teachers
                        if (h > 0) {
                            model.addImplication(timetable[h - 1][index][r][t], timetable[h][index][r][t]);
                            model.addImplication(timetable[h][index][r][t], timetable[h - 1][index][r][t]);
                        }
                        middlePriorityViolations.add(middleViolation);
                    }
                }
            }
        }

        // Objective: Minimize the weighted sum of violation variables
        int highPriorityWeight = 10;
        int middlePriorityWeight = 5;

        LinearExpr highPrioritySum = LinearExpr.sum(highPriorityViolations.toArray(new Literal[0]));
        LinearExpr middlePrioritySum = LinearExpr.sum(middlePriorityViolations.toArray(new Literal[0]));

        model.minimize(LinearExpr.sum(
                new LinearExpr[]{
                        LinearExpr.term(highPrioritySum, highPriorityWeight),
                        LinearExpr.term(middlePrioritySum, middlePriorityWeight)
                }));

        System.out.println("Start Solving: " + LocalDateTime.now());
        CpSolver solver = new CpSolver();
        solver.getParameters().setEnumerateAllSolutions(true);

        VarArraySolutionPrinter cb = new VarArraySolutionPrinter(timetable, courses, map, teachers, numHoursList, DemoData.getTeacherMap(), DemoData.getSubjectMap());
        solver.solve(model, cb);

        System.out.println("\nStatistics");
        System.out.println("  - conflicts      : " + solver.numConflicts());
        System.out.println("  - branches       : " + solver.numBranches());
        System.out.println("  - wall time      : " + solver.wallTime() + " s");
        System.out.println("  - solutions found: " + cb.getSolutionCount());
    }

    public static int combineIds(int subjectId, int classId) {
        return subjectId * 1000 + classId;
    }

    public static int extractSubjectId(int combinedId) {
        return (combinedId / 1000) % 1000;
    }

    public static int extractClassId(int combinedId) {
        return combinedId % 1000;
    }

    static class VarArraySolutionPrinter extends CpSolverSolutionCallback {
        private final BoolVar[][][][] timetable;
        private final List<Integer> courses;
        private final Map<Integer, Integer> map;
        private final List<Integer> teachers;
        private final List<Integer> numHoursList;
        private int solutionCount;
        final HashMap<Integer, Teacher> teacherMap;
        final HashMap<Integer, Subject> subjectMap;

        public VarArraySolutionPrinter(BoolVar[][][][] timetable, List<Integer> courses, Map<Integer, Integer> map, List<Integer> teachers, List<Integer> numHoursList, final HashMap<Integer, Teacher> teacherMap, final HashMap<Integer, Subject> subjectMap) {
            this.solutionCount = 0;
            this.timetable = timetable;
            this.courses = courses;
            this.map = map;
            this.teachers = teachers;
            this.numHoursList = numHoursList;
            this.teacherMap = teacherMap;
            this.subjectMap = subjectMap;
        }

        @Override
        public void onSolutionCallback() {
            solutionCount++;
            System.out.println("Solution " + solutionCount + ":");
            for (int h = 0; h < numHoursList.size(); h++) {
                for (int c = 0; c < courses.size(); c++) {
                    int courseId = courses.get(c);
                    int teacherId = map.get(courseId);
                    for (int i = 0; i < teachers.size(); i++) {
                        if (teachers.get(i) == teacherId) {
                            teacherId = i;
                            break;
                        }
                    }
                    for (int r = 0; r < timetable[0][0].length; r++) {
                        if (booleanValue(timetable[h][c][r][teacherId])) {
                            System.out.println("Hour: " + numHoursList.get(h) +
                                    ", course: " + courses.get(c) +
                                    ", Class: " + extractClassId(courses.get(c)) +
                                    ", Subject: " + subjectMap.getOrDefault(extractSubjectId(courses.get(c)), null).getName() +
                                    ", Room: " + r +
                                    ", Teacher: " + teacherMap.getOrDefault(teachers.get(teacherId), null).getName());
                        }
                    }
                }
            }
        }

        public int getSolutionCount() {
            return solutionCount;
        }
    }
}
