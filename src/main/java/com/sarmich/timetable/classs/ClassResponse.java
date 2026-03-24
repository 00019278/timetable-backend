package com.sarmich.timetable.classs;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;
@Setter
@Getter
public class ClassResponse {

    private Long id;
    private String shortName;
    private String name;
    private List<DayOfWeek> days;
    private Instant createdDate;
}