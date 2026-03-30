package com.sarmich.timetable.model.response;

import com.sarmich.timetable.service.solver.LessonFrequency;
import java.time.DayOfWeek;
import java.util.List;

public record OrTLesson(
    Integer id,
    Integer originalId,
    ClassResponse classInfo,
    TeacherResponse teacher,
    List<RoomResponse> rooms,
    SubjectResponse subject,
    GroupResponse group, // Aniq guruh (null bo'lsa butun sinf)
    String syncId, // <-- YANGI: Parallel darslarni bog'lash uchun
    Integer lessonCount,
    DayOfWeek dayOfWeek,
    Integer hour,
    Integer period,
    LessonFrequency frequency) {}
