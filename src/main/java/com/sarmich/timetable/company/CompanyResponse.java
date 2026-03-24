package com.sarmich.timetable.company;

import java.time.Instant;


public record CompanyResponse(

        Integer id,
        Instant startTime,
        Instant timeLesson,
        Instant breakTime,
        Integer maxLesson
) {
}