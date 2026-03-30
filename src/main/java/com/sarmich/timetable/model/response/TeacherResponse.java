package com.sarmich.timetable.model.response;

import com.sarmich.timetable.model.TimeSlot;
import java.time.Instant;
import java.util.List;

public record TeacherResponse(
    Integer id,
    String fullName,
    String shortName,
    List<SubjectResponse> subjects,
    List<TimeSlot> availabilities,
    Instant createdDate,
    Instant updatedDate) {}
