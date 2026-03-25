package com.sarmich.timetable.model.request;

import jakarta.validation.constraints.NotNull;

public record LessonUpdateRequest(
        Long id,
        Long subjectId,
        Long teacherId,
        Long classId,
        Integer lessonCount
) {
}