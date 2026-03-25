package com.sarmich.timetable.model.response;

import java.time.Instant;


public record CompanyResponse(

        Integer id,
        Instant startTime,
        Instant timeLesson,
        Instant breakTime,
        Integer maxLesson
) {
}