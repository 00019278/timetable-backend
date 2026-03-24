package com.sarmich.timetable.teacher;

import java.time.DayOfWeek;
import java.util.List;

public record TimeSlot(
        DayOfWeek dayOfWeek,
        List<Integer> lessons
) {
}
