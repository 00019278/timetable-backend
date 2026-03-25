package com.sarmich.timetable.model.request;

import com.sarmich.timetable.model.LessonTime;
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