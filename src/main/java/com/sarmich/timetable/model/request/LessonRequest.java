package com.sarmich.timetable.model.request;

import com.sarmich.timetable.service.solver.LessonFrequency;
import java.time.DayOfWeek;
import java.util.List;

public record LessonRequest(
    List<Integer> classId,
    Integer teacherId,
    List<Integer> roomIds,
    Integer subjectId,
    Integer lessonCount,
    DayOfWeek dayOfWeek,
    Integer hour,
    LessonFrequency frequency, // Frontend hisoblab yuboradi
    Integer period,
    List<GroupLessonDetail> groups) {}
