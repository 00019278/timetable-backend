package com.sarmich.timetable.google.or.models;

import java.util.*;

public class Timetable {
     final Teacher[] teachers;  // Use final for immutability
     final Lesson[] lessons;   // Use final for immutability
     final Map<Integer, List<Lesson>> classSchedule; // Map lessons to classes (final for immutability)
     final int numDays;
     final int numSlotsPerDay;

    public Timetable(Teacher[] teachers, Lesson[] lessons, int numDays, int numSlotsPerDay) {
        this.teachers = Arrays.copyOf(teachers, teachers.length);  // Defensive copy
        this.lessons = Arrays.copyOf(lessons, lessons.length);     // Defensive copy
        this.classSchedule = new HashMap<>();
        this.numDays = numDays;
        this.numSlotsPerDay = numSlotsPerDay;
    }

    // Accessor methods (consider adding appropriate methods for retrieving specific information)

    public Teacher[] getTeachers() {
        return Arrays.copyOf(teachers, teachers.length); // Return a defensive copy
    }

    public Lesson[] getLessons() {
        return Arrays.copyOf(lessons, lessons.length); // Return a defensive copy
    }

    public Map<Integer, List<Lesson>> getClassSchedule() {
        return new HashMap<>(classSchedule); // Return a copy to prevent modification
    }

    // Utility methods (consider adding methods for displaying or manipulating the timetable)

    public boolean isLessonAssigned(int lessonId) {
        for (List<Lesson> lessons : classSchedule.values()) {
            for (Lesson lesson : lessons) {
                if (lesson.id == lessonId) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Lesson> getLessonsForDay(int day) {
        return classSchedule.getOrDefault(day, Collections.emptyList());
    }
}
