package com.sarmich.timetable.model.request;

import com.sarmich.timetable.domain.enums.RoomType;
import com.sarmich.timetable.model.TimeSlot;
import java.util.List;

public record RoomRequest(
    String name,
    String shortName,
    RoomType type,
    Integer buildingId,
    List<TimeSlot> availabilities) {}
