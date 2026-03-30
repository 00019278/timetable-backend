package com.sarmich.timetable.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarmich.timetable.service.solver.LessonFrequency;
import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;

public record LessonResponse(
    Integer id,
    @JsonProperty("classId") Integer classId,
    Integer teacherId,
    List<Integer> roomIds,
    Integer subjectId,
    Integer groupId,
    List<GroupLessonDetailResponse> groupDetails,
    Integer lessonCount,
    DayOfWeek dayOfWeek,
    Integer hour,
    Integer period,
    LessonFrequency frequency,
    Instant createdDate,
    Instant updatedDate) {}
