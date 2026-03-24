package com.sarmich.timetable.teacher;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class TeacherRequest {
    private String name;
    private String lastName;
    private Long subjectId;
    private List<TimeSlot> timeSlotList;

    // Constructors, getters, and setters
}
