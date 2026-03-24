package com.sarmich.timetable.subject;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectUpdateDto {

    @NotNull(message = "id required")
    private Long id;

    private String name;
    private String shortName;
    private Integer priority;
}