package com.sarmich.timetable.model.request;

import com.sarmich.timetable.model.LessonTime;
import jakarta.validation.constraints.NotNull;

public record TeacherUpdateRequest(
        @NotNull(message = "id required")
        Integer id,
        String firstName,
        String lastName,
        Integer subjectId,
        LessonTime lessonTimes
) {
}
