package com.sarmich.timetable.model.request;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.util.List;

public record LessonRequest(
    Integer orgId,
    Integer classId,
    Integer teacherId,
    List<Integer> roomIds,
    Integer subjectId,
    Integer lessonCount,
    DayOfWeek dayOfWeek,
    Integer hour,
    Integer period
) {
}
