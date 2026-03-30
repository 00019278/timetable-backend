package com.sarmich.timetable.model;

import java.time.DayOfWeek;
import java.util.List;

public record TimeSlot(DayOfWeek dayOfWeek, List<Integer> lessons) {}
