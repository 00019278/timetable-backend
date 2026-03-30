package com.sarmich.timetable.model.request;

import com.sarmich.timetable.model.TimeSlot;
import java.util.List;
import java.util.Set;

public record ClassRequest(
    String name,
    String shortName,
    List<TimeSlot> availabilities,
    Integer teacherId,
    Set<Integer> rooms,
    List<GroupRequest> groups) {}
