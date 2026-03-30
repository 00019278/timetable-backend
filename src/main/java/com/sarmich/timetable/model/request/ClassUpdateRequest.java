package com.sarmich.timetable.model.request;

import com.sarmich.timetable.model.Days;
import jakarta.validation.constraints.NotNull;

public record ClassUpdateRequest (
    @NotNull(message = "id required")
     Integer id,
     String name,
     String shortName,
     Days days
){}
