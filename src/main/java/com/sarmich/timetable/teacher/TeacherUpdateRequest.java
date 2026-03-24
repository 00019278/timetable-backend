package com.sarmich.timetable.teacher;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TeacherUpdateRequest {
    @NotNull(message = "id required")
    private Long id;
    private String name;
    private String lastName;
    private Long subjectId;
    private List<TimeSlot> timeSlotList;
}