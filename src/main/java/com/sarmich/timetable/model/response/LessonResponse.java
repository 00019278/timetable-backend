package com.sarmich.timetable.model.response;

import com.sarmich.timetable.model.response.LessonInfo;

public record LessonResponse(

        Long id,
        LessonInfo info,
        Integer lessonCount
) {
}