package com.sarmich.timetable.google.or.generate;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import com.sarmich.timetable.google.or.models.Lesson;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;

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
        int numRooms = 30;
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

        for (int c = 0; c < numCourses; c++) {
            ArrayList<Literal> boolVars = new ArrayList<>();
            int courseId = courses.get(c);
            int teacherId = map.get(courseId);
            for (int i = 0; i < teachers.size(); i++) {
                if (teachers.get(i) == teacherId) {
                    teacherId = i;
                    break;
                }
            }
            for (int h = 0; h < numHoursList.size(); h++) {
                for (int r = 0; r < numRooms; r++) {
                    boolVars.add(timetable[h][c][r][teacherId]);
                }
            }
            model.addAtLeastOne(boolVars.toArray(new Literal[0]));
        }

        // New constraint: Ensure each course is scheduled the required number of times per week
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
            ArrayList<Literal> boolVars = new ArrayList<>();
            for (int h = 0; h < numHoursList.size(); h++) {
                for (int r = 0; r < numRooms; r++) {
                    boolVars.add(timetable[h][c][r][teacherId]);
                }
            }
            model.addEquality(LinearExpr.sum(boolVars.toArray(new Literal[0])), requiredCount);
        }

        System.out.println("Start Solving: " + LocalDateTime.now());
        CpSolver solver = new CpSolver();
        solver.getParameters().setEnumerateAllSolutions(true);

        VarArraySolutionPrinter cb = new VarArraySolutionPrinter(timetable, courses, map, teachers, numHoursList);
        solver.solve(model, cb);

        System.out.println("\nStatistics");
        System.out.println("  - conflicts      : " + solver.numConflicts());
        System.out.println("  - branches       : " + solver.numBranches());
        System.out.println("  - wall time      : " + solver.wallTime() + " s");
//        System.out.println("  - solutions found: " + cb.getSolutionCount());
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

        public VarArraySolutionPrinter(BoolVar[][][][] timetable, List<Integer> courses, Map<Integer, Integer> map, List<Integer> teachers, List<Integer> numHoursList) {
            this.solutionCount = 0;
            this.timetable = timetable;
            this.courses = courses;
            this.map = map;
            this.teachers = teachers;
            this.numHoursList = numHoursList;
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
                                    ", Subject: " + extractSubjectId(courses.get(c)) +
                                    ", Room: " + r +
                                    ", Teacher: " + teachers.get(teacherId));
                        }
                    }
                }
            }
        }
    }
}


