package com.sarmich.timetable.model.response;

import com.sarmich.timetable.model.Days;

import java.time.Instant;

public record ClassResponse (

     Long id,
     String shortName,
     String name,
     Days days,
     Instant createdDate
){}