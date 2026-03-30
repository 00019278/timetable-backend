package com.sarmich.timetable.model.response;

import com.sarmich.timetable.model.LessonPeriod;
import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public record CompanyResponse(
    Integer id,
    String name,
    String description,
    Set<DayOfWeek> daysOfWeek,
    List<LessonPeriod> periods,
    Instant createdDate,
    Instant lastModifiedDate) {}
