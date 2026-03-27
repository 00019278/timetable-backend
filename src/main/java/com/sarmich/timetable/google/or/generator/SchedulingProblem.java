//package com.sarmich.timetable.google.or.generator;
//
//import com.google.ortools.Loader;
//import com.google.ortools.sat.*;
//import com.sarmich.timetable.google.or.models.Response;
//import com.sarmich.timetable.google.or.models.Lesson;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class SchedulingProblem {
//    public List<Response> generate(List<Lesson> lessons) {
//        System.out.println("Start : " + LocalDateTime.now());
//        // class
//        List<Integer> classList = lessons.stream().map(Lesson::getClassId).collect(Collectors.toSet()).stream().toList();
//        HashMap<Integer, Integer> classIdMap = new HashMap<>(); // key  - classId ,value -> classning listdagi index
//        HashMap<Integer, Integer> classIndexMap = new HashMap<>(); // key  - classId ,value -> classning listdagi index
//        for (int i = 0; i < classList.size(); i++) {
//            classIdMap.put(classList.get(i), i);
//            classIndexMap.put(i, classList.get(i));
//        }
//
//        // subject
//        List<Integer> subjectList = lessons.stream().map(Lesson::getSubjectId).collect(Collectors.toSet()).stream().toList();
//        HashMap<Integer, Integer> subjectIdMap = new HashMap<>(); // key -subjectId,value ->subjectning listdagi indexi
//        HashMap<Integer, Integer> subjectIndexMap = new HashMap<>(); // key -subjectId,value ->subjectning listdagi indexi
//        for (int i = 0; i < subjectList.size(); i++) {
//            subjectIdMap.put(subjectList.get(i), i);
//            subjectIndexMap.put(i, subjectList.get(i));
//        }
//        // teacher
//        List<Integer> teachers = lessons.stream()
//                .map(Lesson::getTeacherId)
//                .collect(Collectors.toSet()).stream().toList();
//        HashMap<Integer, Integer> teacherIdMap = new HashMap<>();
//        HashMap<Integer, Integer> teacherIndexMap = new HashMap<>();
//        for (int i = 0; i < teachers.size(); i++) {
//            teacherIndexMap.put(i, teachers.get(i));
//            teacherIdMap.put(teachers.get(i), i);
//        }
//
//        Loader.loadNativeLibraries();
//        CpModel model = new CpModel();
//
//        int roomCount = 20;
//        int hoursCount = 29;
//
//        BoolVar[][][][][] timetable = createTimetableVariablesWithClass(model, hoursCount, classIndexMap, roomCount, teacherIdMap, subjectIdMap, lessons);
////        addTeacherConstraintsWithClass(model, hoursCount, teacherIndexMap, roomCount, lessons, classIndexMap, subjectIdMap, timetable, teacherIdMap);
//
//        // Add constraints to the model
//        for (int r = 0; r < roomCount; r++) {
//            for (int h = 0; h < hoursCount; h++) {
//                // Ensure that each class has only one subject at a time
//                for (int c = 0; c < classIndexMap.size(); c++) {
//                    List<BoolVar> classLessonsAtHour = new ArrayList<>();
//                    for (int t = 0; t < teacherIndexMap.size(); t++) {
//                        for (int s = 0; s < subjectIndexMap.size(); s++) {
//                            if (timetable[c][r][t][h][s] != null) {
//                                classLessonsAtHour.add(timetable[c][r][t][h][s]);
//                            }
//                        }
//                    }
//                    model.addAtMostOne(classLessonsAtHour.toArray(new BoolVar[0]));
//                }
//
//                // Ensure that each teacher can only teach one class at a time
//                for (int t = 0; t < teacherIndexMap.size(); t++) {
//                    List<BoolVar> teacherLessonsAtHour = new ArrayList<>();
//                    for (int c = 0; c < classIndexMap.size(); c++) {
//                        for (int s = 0; s < subjectIndexMap.size(); s++) {
//                            if (timetable[c][r][t][h][s] != null) {
//                                teacherLessonsAtHour.add(timetable[c][r][t][h][s]);
//                            }
//                        }
//                    }
//                    model.addAtMostOne(teacherLessonsAtHour.toArray(new BoolVar[0]));
//                }
//            }
//        }
//
//// Add additional constraints as necessary
//
//        return solveAndGenerateTimetable(model, lessons, hoursCount, subjectIndexMap, teacherIndexMap, classIndexMap, timetable, roomCount);
////        addRoomConstraints(model, hoursCount, teacherIndexMap, roomCount, lessons, classIdMap, subjectIdMap, timetable);
////        addClassSubjectConstraints(model, numHoursList, numCourses, roomCount, courseTeacherMap, teachers, timetable, courses);
////        addRoomConstraints(model, numHoursList, numCourses, roomCount, courses, courseTeacherMap, teachers, timetable);
////        addWeeklyCourseConstraints(model, lessons, numHoursList, courses, courseTeacherMap, teachers, timetable);
////        addNoBreakConstraints(model, numHoursList, numCourses, roomCount, courses, courseTeacherMap, teachers, timetable);
//
//    }
//
//    private BoolVar[][][][][] createTimetableVariablesWithClass(CpModel model, Integer hourCount, HashMap<Integer, Integer> classIndexMap, int roomCount, HashMap<Integer, Integer> teacherMap, HashMap<Integer, Integer> subjectMap, List<Lesson> lessons) {
//        BoolVar[][][][][] timetable = new BoolVar[classIndexMap.size()][roomCount][teacherMap.size()][hourCount][subjectMap.size()];
////        List<String> result = new ArrayList<>();
//
//        for (int r = 0; r < roomCount; r++) {
//            for (int h = 0; h < hourCount; h++) {
//                for (int c = 0; c < classIndexMap.size(); c++) {
//                    Integer classId = classIndexMap.get(c);
//                    List<Integer> teachers = lessons.stream().filter(l -> Objects.equals(l.getClassId(), classId)).collect(Collectors.toSet()).stream().map(Lesson::getTeacherId).toList();
//                    for (int teacherId : teachers) {
//                        Integer t = teacherMap.get(teacherId);
//                        List<Integer> subjects = lessons.stream().filter(l -> Objects.equals(l.getClassId(), classId) && Objects.equals(l.getTeacherId(), teacherId)).collect(Collectors.toSet()).stream().map(Lesson::getSubjectId).toList();
//                        for (int subjectId : subjects) {
//                            int s = subjectMap.get(subjectId);
//                            String str = "c" + c + "_r" + r + "_t" + t + "_h" + h + "_s" + s;
////                            result.add(str);
//                            timetable[c][r][t][h][s] = model.newBoolVar(str);
//                        }
//                    }
//                }
//            }
//        }
////        result.stream().sorted().forEach(System.out::println);
//        return timetable;
//    }
//
//
//    /**
//     * timetable[3][3][3][3][3];
//     * .........................
//     * timetable[0][0] [0][0][0]
//     * timetable[0][0] [0][0][1]
//     * timetable[0][0] [0][0][2]
//     * timetable[0][0] [1][0][2]
//     * timetable[0][0] [1][0][1]
//     * timetable[0][0] [1][0][0]
//     * ..... shulardan faqatgina bittasi true bolishi kerak shari
//     */
//    private void addTeacherConstraintsWithClass(CpModel model, Integer hourCount, Map<Integer, Integer> teacherMap, int roomCount, List<Lesson> lessons, Map<Integer, Integer> classIndexMap, Map<Integer, Integer> subjectIdMap, BoolVar[][][][][] timetable, final HashMap<Integer, Integer> teacherIdMap) {
//        for (int c = 0; c < classIndexMap.size(); c++) {
//            int classId = classIndexMap.get(c); // Fix index access
//            for (int h = 0; h < hourCount; h++) {
//                List<Integer> classTeachers = lessons.stream().filter(l -> l.getClassId() == classId).map(Lesson::getTeacherId).collect(Collectors.toSet()).stream().toList();
//                ArrayList<Literal> boolVars = new ArrayList<>();
//                for (int teacherId : classTeachers) {
//                    Integer t = teacherIdMap.get(teacherId);
//                    List<Integer> classSubjects = lessons.stream().filter(l -> l.getTeacherId() == teacherId && l.getClassId() == classId).map(Lesson::getSubjectId).collect(Collectors.toSet()).stream().toList();
//                    for (int subjectId : classSubjects) {
//                        int s = subjectIdMap.get(subjectId);
//                        for (int r = 0; r < roomCount; r++) {
//                            if (timetable[c][r][t][h][s] != null) {
//                                boolVars.add(timetable[c][r][t][h][s]);
//                            } else {
//                                System.out.println("Null detected at: c=" + c + ", r=" + r + ", t=" + t + ", h=" + h + ", s=" + s);
//                            }
//                        }
//                    }
//                }
//                System.out.println("Adding at most one constraint for class " + classId + " at hour " + h);
//                model.addAtMostOne(boolVars.toArray(new Literal[0]));
//            }
//        }
//    }
//
//
//    private void addRoomConstraints(CpModel model, Integer hourCount, Map<Integer, Integer> teacherMap, int roomCount, List<Lesson> lessons, Map<Integer, Integer> classMap, Map<Integer, Integer> subjectMap, BoolVar[][][][][] timetable) {
//        for (int r = 0; r < roomCount; r++) {
//            for (int h = 0; h < hourCount; h++) {
//                ArrayList<Literal> boolVars = new ArrayList<>();
//                for (int t = 0; t < teacherMap.size(); t++) {
//                    // teacherni db dagi id
//                    int teacherId = teacherMap.get(t);
//                    // teacherni dars beradigan darslari
//                    List<Integer> classes = lessons.stream().filter(l -> l.getTeacherId() == teacherId).map(Lesson::getClassId).toList();
//                    for (int classId : classes) {
//                        // shu class va shu sinf uchun subjectlar
//                        List<Integer> subjects = lessons.stream().filter(l -> l.getTeacherId() == teacherId && l.getClassId() == classId).map(Lesson::getClassId).toList();
//                        for (int subjectId : subjects) {
//                            int sId = subjectMap.get(subjectId);
//                            boolVars.add(timetable[classMap.get(classId)][r][teacherId][h][sId]);
//                        }
//                    }
//                }
//                // Add the constraint that at most one lesson can be scheduled in this room at this hour
//                model.addAtMostOne(boolVars.toArray(new Literal[0]));
//            }
//        }
//    }
//
//
//    private void addClassSubjectConstraints(CpModel model, int hourCount, int classCount, int roomCount, List<Lesson> lessons, BoolVar[][][][][] timetable) {
//        // Create a map of class to its subjects
//        Map<Integer, Set<Integer>> classSubjectsMap = new HashMap<>();
//        for (Lesson lesson : lessons) {
//            int classId = lesson.getClassId();
//            int subjectId = lesson.getSubjectId();
//            classSubjectsMap.putIfAbsent(classId, new HashSet<>());
//            classSubjectsMap.get(classId).add(subjectId);
//        }
//
//        // Iterate over each class
//        for (int c = 0; c < classCount; c++) {
//            // Get the subjects for the current class
//            Set<Integer> classSubjects = classSubjectsMap.getOrDefault(c, Collections.emptySet());
//
//            // Iterate over each hour
//            for (int h = 0; h < hourCount; h++) {
//                ArrayList<Literal> boolVars = new ArrayList<>();
//
//                // Iterate over each subject that is actually taught in the class
//                for (int s : classSubjects) {
//                    // Iterate over each room
//                    for (int r = 0; r < roomCount; r++) {
//                        // Collect all variables for the current class, hour, and subject across all rooms
//                        if (timetable[c][r][0][h][s] != null) {
//                            boolVars.add(timetable[c][r][0][h][s]);
//                        }
//                    }
//                }
//                // Add constraint that at most one subject can be scheduled for the class at this hour
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
//    private List<Response> solveAndGenerateTimetable(CpModel model, List<Lesson> lessons, Integer hourCount, Map<Integer, Integer> subjectIndexMap, Map<Integer, Integer> teacherIndexMap, Map<Integer, Integer> classIndexMap, BoolVar[][][][][] timetable, final int roomCount) {
//        System.out.println("Start Solving: " + LocalDateTime.now());
//        CpSolver solver = new CpSolver();
//        CpSolverStatus status = solver.solve(model);
//
//        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
//            System.out.println("Solution found:");
//            List<Response> timetables = new ArrayList<>();
//
//            for (int c = 0; c < classIndexMap.size(); c++) {
//                int classId = classIndexMap.get(c);
//                for (int r = 0; r < roomCount; r++) {
//                    for (int t = 0; t < teacherIndexMap.size(); t++) {
//                        int teacherId = teacherIndexMap.get(t);
//                        for (int h = 0; h < hourCount; h++) {
//                            for (int s = 0; s < subjectIndexMap.size(); s++) {
//                                int subjectId = subjectIndexMap.get(s);
//                                if (timetable[c][r][t][h][s] != null) {
//                                    if (solver.booleanValue(timetable[c][r][t][h][s])) {
//                                        Response response = new Response();
////                                        response.setClassId(classId);
////                                        response.setRoom(r);
////                                        response.setTeacherId(teacherId);
////                                        response.setHour(h);
////                                        response.setSubjectId(subjectId);
//                                        timetables.add(response);
//
//                                        System.out.printf("Assigned: Class %d, Room %d, Teacher %d, Hour %d, Subject %d%n", classId, r, teacherId, h, subjectId);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            System.out.println("\nStatistics");
//            System.out.println("  - conflicts      : " + solver.numConflicts());
//            System.out.println("  - branches       : " + solver.numBranches());
//            System.out.println("  - wall time      : " + solver.wallTime() + " s");
//
//            return timetables;
//        }
//
//        System.out.println("No solution found.");
//        return null;
//    }
//
//
//    private <K, V> K getKeyByValue(Map<K, V> map, V value) {
//        for (Map.Entry<K, V> entry : map.entrySet()) {
//            if (Objects.equals(value, entry.getValue())) {
//                return entry.getKey();
//            }
//        }
//        return null;
//    }
//
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
