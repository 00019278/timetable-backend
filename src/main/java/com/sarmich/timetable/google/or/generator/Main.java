package com.sarmich.timetable.google.or.generator;

import com.sarmich.timetable.google.or.models.DemoData;
import com.sarmich.timetable.google.or.models.Response;
import com.sarmich.timetable.google.or.writer.TimetableExporter;

import java.util.HashSet;
import java.util.List;

public class Main {
  public static void main(String[] args) throws Exception {
    SolverClone solver = new SolverClone();
    List<Response> generate =
        solver.generate(new HashSet<>(DemoData.lessonList()).stream().toList());
    TimetableExporter.exportToExcel(generate, "timetable.xlsx", 6);
  }
}
