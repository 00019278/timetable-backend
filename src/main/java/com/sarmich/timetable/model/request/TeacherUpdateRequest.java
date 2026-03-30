package com.sarmich.timetable.model.request;

import com.sarmich.timetable.model.TimeSlot;
import java.util.List;

public record TeacherUpdateRequest(
    String fullName,
    String shortName,
    List<Integer> subjects,
    List<Integer> deletedSubjects,
    List<TimeSlot> availabilities) {}
