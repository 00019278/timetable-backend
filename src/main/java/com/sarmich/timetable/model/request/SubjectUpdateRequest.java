package com.sarmich.timetable.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public record SubjectUpdateRequest(

        @NotNull(message = "id required")
        Integer id,

        String name,
        String shortName,
        Integer priority
) {
}
