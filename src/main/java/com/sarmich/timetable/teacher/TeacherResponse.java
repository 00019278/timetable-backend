package com.sarmich.timetable.teacher;

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
