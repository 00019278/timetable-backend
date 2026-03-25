package com.sarmich.timetable.model.request;


import com.sarmich.timetable.model.LessonTime;

public record TeacherRequest (
     String firstName,
     String lastName,
     Long subjectId,
     LessonTime lessonTimes
){}
