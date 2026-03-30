package com.sarmich.timetable.model.response;

import com.sarmich.timetable.model.TimeSlot;

import java.util.List;

public record SubjectResponse(
    Long id,
    String shortName,
    String name,
    List<TimeSlot> availabilities
) {
}
