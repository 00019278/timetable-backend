package com.sarmich.timetable.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public record SubjectRequest(

        @NotBlank(message = "short-firstName must be at least 2")
        @Size(min = 2, message = "short-firstName must be at least 2")
        String shortName,

        @Size(min = 4, message = "firstName must be at least 4")
        String name,

        @Max(value = 10, message = "priority between 1 and 10")
        @Min(value = 1, message = "priority between 1 and 10")
        Integer priority

) {
}