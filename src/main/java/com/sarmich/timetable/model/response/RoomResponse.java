package com.sarmich.timetable.model.response;

import com.sarmich.timetable.domain.enums.RoomType;
import com.sarmich.timetable.model.TimeSlot;
import java.util.List;

public record RoomResponse(
    Integer id,
    String name,
    String shortName,
    RoomType type,
    BuildingResponse building,
    List<TimeSlot> availabilities) {}
