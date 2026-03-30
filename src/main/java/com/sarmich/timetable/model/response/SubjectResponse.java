package com.sarmich.timetable.model.response;

import com.sarmich.timetable.model.TimeSlot;
import java.time.Instant;
import java.util.List;

public record SubjectResponse(
    Integer id,
    String shortName,
    String name,
    List<TimeSlot> availabilities,
    String emoji,
    String color,
    Integer weight,
    Instant createdDate,
    Instant updatedDate) {}
