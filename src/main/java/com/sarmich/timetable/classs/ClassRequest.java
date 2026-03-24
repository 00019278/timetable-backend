package com.sarmich.timetable.classs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.List;
public record ClassRequest (

    @NotBlank(message = "short-firstName must be at least 2")
    @Size(min = 2, message = "short-firstName must be at least 2")
     String shortName,

    @Size(min = 2, message = "firstName must be at least 4")
     String name,

     Days days
){}