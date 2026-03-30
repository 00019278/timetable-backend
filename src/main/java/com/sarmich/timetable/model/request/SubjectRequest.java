package com.sarmich.timetable.model.request;

import com.sarmich.timetable.model.TimeSlot;
import java.util.List;

public record SubjectRequest(
    String shortName,
    String name,
    List<TimeSlot> availabilities,
    String emoji,
    String color,
    Integer weight) {}
