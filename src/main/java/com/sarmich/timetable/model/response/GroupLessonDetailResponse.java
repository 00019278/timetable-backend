package com.sarmich.timetable.model.response;

import java.util.List;

public record GroupLessonDetailResponse(
    Integer groupId,
    Integer teacherId,
    Integer subjectId,
    List<Integer> roomIds) {}
