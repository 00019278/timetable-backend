package com.sarmich.timetable.google.or.generate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Class {
    Integer id;

    public Class() {
    }

    String name;

    public Class(final Integer id, final String name) {
        this.id = id;
        this.name = name;
    }
}
