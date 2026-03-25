package com.sarmich.timetable.lesson;

import com.sarmich.timetable.classs.ClassResponse;
import com.sarmich.timetable.subject.SubjectResponse;
import com.sarmich.timetable.teacher.TeacherResponse;

public record LessonInfo(
        TeacherResponse teacher,
        SubjectResponse subject,
        ClassResponse classResponse
) {
}
