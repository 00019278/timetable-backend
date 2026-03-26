package com.sarmich.timetable.google.or.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Subject {
    Integer id;
    String name;
    Integer priority;

    public Subject(final Integer id, final String name) {
        this.id = id;
        this.name = name;
    }
}
