package com.sarmich.timetable.subject;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public record SubjectUpdateRequest(

        @NotNull(message = "id required")
        Long id,

        String name,
        String shortName,
        Integer priority
) {
}