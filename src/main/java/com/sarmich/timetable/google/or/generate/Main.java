package com.sarmich.timetable.google.or.generate;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        SchedulingProblem problem = new SchedulingProblem();
        List<Response> generate = problem.generate(DemoData.lessonList());
        Writer.writer(generate);
    }
}
