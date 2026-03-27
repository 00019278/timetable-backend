package com.sarmich.timetable.google.or.example;

import com.google.ortools.sat.*;

import java.time.DayOfWeek;
import java.util.*;

public class SchoolTimetableGenerator {

    static class TimeSlot {
        DayOfWeek day;
        Integer value;
        Integer priority;

        TimeSlot(DayOfWeek day, Integer value, Integer priority) {
            this.day = day;
            this.value = value;
            this.priority = priority;
        }
    }

    static class Class {
        Integer id;
        String name;

        Class(Integer id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class Teacher {
        Integer id;
        String name;
        List<TimeSlot> timeSlots;

        Teacher(Integer id, String name, List<TimeSlot> timeSlots) {
            this.id = id;
            this.name = name;
            this.timeSlots = timeSlots;
        }
    }

    static class Subject {
        Integer id;
        String name;
        Integer priority;

        Subject(Integer id, String name, Integer priority) {
            this.id = id;
            this.name = name;
            this.priority = priority;
        }
    }

    static class Lesson {
        Integer id;
        Integer count;
        Integer teacherId;
        Integer subjectId;
        Integer classId;

        Lesson(Integer id, Integer count, Integer teacherId, Integer subjectId, Integer classId) {
            this.id = id;
            this.count = count;
            this.teacherId = teacherId;
            this.subjectId = subjectId;
            this.classId = classId;
        }
    }

    public static void main(String[] args) {
        // Example data
        List<TimeSlot> timeSlotsTeacher1 = Arrays.asList(
                new TimeSlot(DayOfWeek.MONDAY, 1, 3),
                new TimeSlot(DayOfWeek.WEDNESDAY, 2, 2)
        );

        List<TimeSlot> timeSlotsTeacher2 = Arrays.asList(
                new TimeSlot(DayOfWeek.TUESDAY, 1, 2),
                new TimeSlot(DayOfWeek.THURSDAY, 2, 3)
        );

        List<Teacher> teachers = Arrays.asList(
                new Teacher(1, "Teacher A", timeSlotsTeacher1),
                new Teacher(2, "Teacher B", timeSlotsTeacher2)
        );

        List<Class> classes = Arrays.asList(
                new Class(1, "Class A"),
                new Class(2, "Class B")
        );

        List<Subject> subjects = Arrays.asList(
                new Subject(1, "Math", 3),
                new Subject(2, "Physics", 2)
        );

        List<Lesson> lessons = Arrays.asList(
                new Lesson(1, 2, 1, 1, 1),
                new Lesson(2, 1, 2, 2, 2),
                new Lesson(3, 1, 1, 2, 1)
        );

        // Generate school timetable
        Map<Lesson, Teacher> timetable = generateSchoolTimetable(teachers, classes, subjects, lessons);

        // Print timetable
        if (timetable != null) {
            System.out.println("Generated School Timetable:");
            for (Map.Entry<Lesson, Teacher> entry : timetable.entrySet()) {
                Lesson lesson = entry.getKey();
                Teacher teacher = entry.getValue();
                System.out.println("Lesson " + lesson.id + " taught by Teacher " + teacher.id);
            }
        }
    }

    public static Map<Lesson, Teacher> generateSchoolTimetable(List<Teacher> teachers, List<Class> classes,
                                                               List<Subject> subjects, List<Lesson> lessons) {
        CpModel model = new CpModel();
        CpSolver solver = new CpSolver();

        // Decision variables
        Map<Lesson, Map<Teacher, IntVar>> lessonAssignments = new HashMap<>();
        for (Lesson lesson : lessons) {
            lessonAssignments.put(lesson, new HashMap<>());
            for (Teacher teacher : teachers) {
                if (teacher.timeSlots.stream().anyMatch(slot -> slot.day.equals(DayOfWeek.of(lesson.id)))) {
                    lessonAssignments.get(lesson).put(teacher, model.newBoolVar(
                            "lesson_" + lesson.id + "_assigned_to_teacher_" + teacher.id
                    ));
                }
            }
        }

        // Constraints
        for (Teacher teacher : teachers) {
            for (Lesson lesson : lessons) {
                if (lessonAssignments.get(lesson).containsKey(teacher)) {
                    model.addLessOrEqual(
                            LinearExpr.sum(lessonAssignments.get(lesson).values().toArray(new IntVar[0])),
                            1
                    ); // Each teacher can teach at most one lesson at a time
                }
            }
        }

        for (Lesson lesson : lessons) {
            if (lessonAssignments.get(lesson).size() > 0) {
                model.addEquality(
                        LinearExpr.sum(lessonAssignments.get(lesson).values().toArray(new IntVar[0])),
                        1
                ); // Each lesson must be taught by exactly one teacher
            }
        }

        // Solve the model
        CpSolverStatus status = solver.solve(model);
        if (status == CpSolverStatus.FEASIBLE) {
            Map<Lesson, Teacher> timetable = new HashMap<>();
            for (Lesson lesson : lessons) {
                for (Teacher teacher : teachers) {
                    if (lessonAssignments.get(lesson).containsKey(teacher) &&
                            solver.value(lessonAssignments.get(lesson).get(teacher)) == 1) {
                        timetable.put(lesson, teacher);
                    }
                }
            }
            return timetable;
        } else {
            System.out.println("No feasible solution found.");
            return null;
        }
    }

}
