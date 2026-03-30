package com.sarmich.timetable.model.request;


import com.sarmich.timetable.model.LessonTime;

public record TeacherRequest (
     String firstName,
     String lastName,
     Integer subjectId,
     LessonTime lessonTimes
){}
