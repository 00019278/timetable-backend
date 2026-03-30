package com.sarmich.timetable.model.request;

import com.sarmich.timetable.model.LessonPeriod;
import java.time.DayOfWeek;
import java.util.List;

public record CompanyRequest(
    String name, String description, List<DayOfWeek> daysOfWeek, List<LessonPeriod> periods) {}
