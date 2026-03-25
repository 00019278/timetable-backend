package com.sarmich.timetable.model;

import java.util.List;

public record LessonTime(
        List<TimeSlot> times
) {
}
