package com.sarmich.timetable.classs;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.List;
@Getter
@Setter
public class ClassUpdateRequest {

    @NotNull(message = "id required")
    private Long id;
    private String name;
    private String shortName;
    private List<DayOfWeek> days;
}
