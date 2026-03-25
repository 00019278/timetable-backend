package com.sarmich.timetable.model.response;

import com.sarmich.timetable.model.LessonTime;

import java.time.Instant;

public record TeacherResponse(
        Long id,
        String firstName,
        String lastName,
        Long subjectId,
        LessonTime lessonTimes,
        Instant createdDate,
        Instant updatedDate

) {
}
