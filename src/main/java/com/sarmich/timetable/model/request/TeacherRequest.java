package com.sarmich.timetable.model.request;

import com.sarmich.timetable.model.TimeSlot;
import java.util.List;

public record TeacherRequest(
    String fullName, String shortName, List<Integer> subjects, List<TimeSlot> availabilities) {}
