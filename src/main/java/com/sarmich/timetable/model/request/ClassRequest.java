package com.sarmich.timetable.model.request;

import com.sarmich.timetable.model.Days;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClassRequest (

    @NotBlank(message = "short-firstName must be at least 2")
    @Size(min = 2, message = "short-firstName must be at least 2")
     String shortName,

    @Size(min = 2, message = "firstName must be at least 4")
     String name,

     Days days
){}