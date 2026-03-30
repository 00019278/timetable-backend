package com.sarmich.timetable.model.response;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;

public record LessonResponse(
        Integer id,
        Integer orgId,
        ClassResponse classInfo,
        TeacherResponse teacherInfo,
        List<RoomResponse> rooms,
        SubjectResponse subjectInfo,
        Integer lessonCount,
        DayOfWeek dayOfWeek,
        Integer hour,
        Integer period,
        Instant createdDate,
        Instant updatedDate
) {}
