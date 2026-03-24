package com.sarmich.timetable.subject;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SubjectRequest {

    @NotBlank(message = "short-name must be at least 2")
    @Size(min = 2, message = "short-name must be at least 2")
    private String shortName;

    @Size(min = 4, message = "name must be at least 4")
    private String name;

    @Max(value = 10, message = "priority between 1 and 10")
    @Min(value = 1, message = "priority between 1 and 10")
    private Integer priority;

    // Constructors, getters, setters, etc.
}