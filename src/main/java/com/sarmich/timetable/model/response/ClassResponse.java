package com.sarmich.timetable.model.response;

import com.sarmich.timetable.model.TimeSlot;
import java.time.Instant;
import java.util.List;

public record ClassResponse(
    Integer id,
    String shortName,
    String name,
    List<TimeSlot> availabilities,
    TeacherResponse teacher,
    List<RoomResponse> rooms,
    List<GroupResponse> groups,
    Instant updatedDate,
    Instant createdDate) {}
