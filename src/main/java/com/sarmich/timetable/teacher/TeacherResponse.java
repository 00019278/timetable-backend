package com.sarmich.timetable.teacher;

import java.time.Instant;
import java.util.List;

public class TeacherResponse {
    private Long id;
    private String name;
    private String lastName;
    private Long subjectId;
    private List<TimeSlot> timeSlotList;
    private Instant createdDate;
    private Instant updatedDate;

    // Constructors, getters, and setters
}
