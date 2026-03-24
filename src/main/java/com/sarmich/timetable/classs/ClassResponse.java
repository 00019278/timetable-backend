package com.sarmich.timetable.classs;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;
public record ClassResponse (

     Long id,
     String shortName,
     String name,
     Days days,
     Instant createdDate
){}