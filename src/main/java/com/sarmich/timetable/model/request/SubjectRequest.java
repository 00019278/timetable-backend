package com.sarmich.timetable.model.request;

import com.sarmich.timetable.model.TimeSlot;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public record SubjectRequest(
    String shortName,
    String name,
    List<TimeSlot> availabilities
) {
}
