package com.sarmich.timetable.model.request;

import java.util.List;

public record GroupLessonDetail(
    Integer groupId, Integer teacherId, Integer subjectId, List<Integer> roomIds) {}
