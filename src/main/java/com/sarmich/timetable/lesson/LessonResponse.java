package com.sarmich.timetable.lesson;

import com.sarmich.timetable.classs.ClassResponse;
import com.sarmich.timetable.subject.SubjectResponse;
import com.sarmich.timetable.teacher.TeacherResponse;

public record LessonResponse(

        Long id,
        LessonInfo info,
        Integer lessonCount
) {
}