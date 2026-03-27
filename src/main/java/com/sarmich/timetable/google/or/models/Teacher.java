package com.sarmich.timetable.google.or.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class Teacher {
    Integer id;
    String name;

    public Teacher() {
    }

    public Teacher(final Integer id, final String name) {
        this.id = id;
        this.name = name;
    }
//    List<TimeSlot> timeSlot;
}
