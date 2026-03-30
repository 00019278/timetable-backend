package com.sarmich.timetable.model.request;

import com.sarmich.timetable.model.TimeSlot;
import java.util.List;

public record ApplyToOthersRequest(List<Integer> applyTo, List<TimeSlot> timeOff) {}
