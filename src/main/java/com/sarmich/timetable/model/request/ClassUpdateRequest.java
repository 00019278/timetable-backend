package com.sarmich.timetable.model.request;

import com.sarmich.timetable.model.TimeSlot;
import java.util.List;
import java.util.Set;

public record ClassUpdateRequest(
    String name,
    String shortName,
    List<TimeSlot> availabilities,
    Integer teacherId,
    Set<Integer> rooms,
    Set<Integer> deletedRooms,
    List<GroupRequest> newGroups,
    List<GroupUpdateRequest> updatedGroups,
    Set<Integer> deletedGroupIds) {}
