package com.sarmich.timetable.model.response;

public record LessonInfo(
        TeacherResponse teacher,
        SubjectResponse subject,
        ClassResponse classResponse
) {
}
