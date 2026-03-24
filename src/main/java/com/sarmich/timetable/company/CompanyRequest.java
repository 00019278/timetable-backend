package com.sarmich.timetable.company;

import java.time.Instant;

public record CompanyRequest(
        Instant startTime,
        Instant timeLesson,
        Instant breakTime,
        Integer maxLesson
){}