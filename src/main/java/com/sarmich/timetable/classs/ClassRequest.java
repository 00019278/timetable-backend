package com.sarmich.timetable.classs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.List;
@Getter
@Setter
public class ClassRequest {

    @NotBlank(message = "short-name must be at least 2")
    @Size(min = 2, message = "short-name must be at least 2")
    private String shortName;

    @Size(min = 2, message = "name must be at least 4")
    private String name;

    private List<DayOfWeek> days;
}