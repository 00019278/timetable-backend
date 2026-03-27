package com.sarmich.timetable.google.or.generate;

import com.sarmich.timetable.google.or.models.Subject;
import com.sarmich.timetable.google.or.models.Teacher;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;

@Getter
@Setter
public class Response {
    DayOfWeek day;
    Integer hour;

    public Response() {
    }

    Teacher teacher;
    Subject subject;
    Class aClass;

    public Response(final DayOfWeek day, final Integer hour, final Teacher teacher, final Subject subject, final Class aClass) {
        this.day = day;
        this.hour = hour;
        this.teacher = teacher;
        this.subject = subject;
        this.aClass = aClass;
    }
}
