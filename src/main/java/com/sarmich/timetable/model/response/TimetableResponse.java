package com.sarmich.timetable.model.response;

import java.time.Instant;
import java.util.UUID;

public record TimetableResponse(
    UUID id,
    UUID taskId,
    String name,
    Integer scheduled,
    Integer unscheduled,
    Integer score,
    Integer teacherGaps,
    Integer classGaps,
    Boolean deleted,
    Instant createdDate,
    Instant updatedDate) {}
