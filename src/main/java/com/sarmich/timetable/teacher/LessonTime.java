package com.sarmich.timetable.teacher;

import java.util.List;

public record LessonTime(
        List<TimeSlot> times
) {
}
