package com.sarmich.timetable.google.or.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Lesson {
    Integer id;
    Integer count;
    Integer teacherId;
    Integer subjectId;
    Integer classId;
    public Lesson(final Integer id, final Integer count, final Integer teacherId, final Integer subjectId, final Integer classId) {
        this.id = id;
        this.count = count;
        this.teacherId = teacherId;
        this.subjectId = subjectId;
        this.classId = classId;
    }


}
