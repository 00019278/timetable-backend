package com.sarmich.timetable.model;

import com.sarmich.timetable.model.response.*;

import java.time.DayOfWeek;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    DayOfWeek day;
    Integer hour;
    TeacherResponse teacher;
    SubjectResponse subject;
    ClassResponse classObj;
    GroupResponse group;
    RoomResponse room;
    Integer period;
    Integer weekIndex; // 0=A hafta, 1=B hafta, null=Har hafta (WEEKLY)
    Integer lessonId;
}
