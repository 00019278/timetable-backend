package com.sarmich.timetable.classs;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.List;
public record ClassUpdateRequest (
    @NotNull(message = "id required")
     Long id,
     String name,
     String shortName,
     Days days
){}