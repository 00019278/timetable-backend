package com.sarmich.timetable.google.or.generate;

public class Main {
    public static void main(String[] args) {
        DemoData.getSubjectMap();
        SchedulingProblem.generate(DemoData.lessonList());
    }
}
