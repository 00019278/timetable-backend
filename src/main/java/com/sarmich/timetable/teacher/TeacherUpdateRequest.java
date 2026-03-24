package com.sarmich.timetable.teacher;

import jakarta.validation.constraints.NotNull;

public record TeacherUpdateRequest(
        @NotNull(message = "id required")
        Long id,
        String firstName,
        String lastName,
        Long subjectId,
        LessonTime lessonTimes
) {
}