package com.sarmich.timetable.model.request;

import com.sarmich.timetable.model.LessonPeriod;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record CompanyRequest(
    String name, String description, Set<DayOfWeek> daysOfWeek, List<LessonPeriod> periods) {

  public CompanyRequest {
    if (daysOfWeek == null || daysOfWeek.isEmpty()) {
      daysOfWeek =
          Set.of(
              DayOfWeek.MONDAY,
              DayOfWeek.TUESDAY,
              DayOfWeek.WEDNESDAY,
              DayOfWeek.THURSDAY,
              DayOfWeek.FRIDAY,
              DayOfWeek.SATURDAY);
    }
    if (periods == null || periods.isEmpty()) {
      periods = new ArrayList<>();
      periods.add(new LessonPeriod("1-soat", "08:00", "08:45", 45, false));
      periods.add(new LessonPeriod("Tanaffus", "08:45", "08:55", 10, true));
      periods.add(new LessonPeriod("2-soat", "08:55", "09:40", 45, false));
      periods.add(new LessonPeriod("Tanaffus", "09:40", "09:50", 10, true));
      periods.add(new LessonPeriod("3-soat", "09:50", "10:35", 45, false));
      periods.add(new LessonPeriod("Tanaffus", "10:35", "10:45", 10, true));
      periods.add(new LessonPeriod("4-soat", "10:45", "11:30", 45, false));
      periods.add(new LessonPeriod("Katta tanaffus", "11:30", "11:50", 20, true));
      periods.add(new LessonPeriod("5-soat", "11:50", "12:35", 45, false));
      periods.add(new LessonPeriod("Tanaffus", "12:35", "12:45", 10, true));
      periods.add(new LessonPeriod("6-soat", "12:45", "13:30", 45, false));
      periods.add(new LessonPeriod("Tanaffus", "13:30", "13:40", 10, true));
      periods.add(new LessonPeriod("7-soat", "13:40", "14:25", 45, false));
    }
  }
}
