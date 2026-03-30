package com.sarmich.timetable.model.request;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.util.List;

public record LessonUpdateRequest(
        @NotNull Integer id,
        @NotNull Integer orgId,
        @NotNull Integer classId,
        @NotNull Integer teacherId,
        @NotNull List<Integer> roomIds,
        @NotNull Integer subjectId,
        @NotNull Integer lessonCount,
        @NotNull DayOfWeek dayOfWeek,
        @NotNull Integer hour,
        @NotNull Integer period
) {
}
