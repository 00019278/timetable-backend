package com.sarmich.timetable.model.request;

import jakarta.validation.constraints.NotNull;

public record SubjectUpdateRequest(
    @NotNull(message = "id required") Integer id,
    String name,
    String shortName,
    Integer priority) {}
