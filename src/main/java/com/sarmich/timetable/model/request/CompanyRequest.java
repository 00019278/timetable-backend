package com.sarmich.timetable.model.request;

import java.time.Instant;

public record CompanyRequest(
        Instant startTime,
        Instant timeLesson,
        Instant breakTime,
        Integer maxLesson
){}