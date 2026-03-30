package com.sarmich.timetable.model.request;

import com.sarmich.timetable.model.TimeSlot;
import java.util.List;

public record RoomUpdateRequest(String name, String shortName, List<TimeSlot> availabilities) {}
