package com.sarmich.timetable.model;

import java.time.DayOfWeek;
import java.util.List;

public record Days(
        List<DayOfWeek> days
) {
}
