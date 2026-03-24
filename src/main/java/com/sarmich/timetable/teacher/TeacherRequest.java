package com.sarmich.timetable.teacher;


public record TeacherRequest (
     String firstName,
     String lastName,
     Long subjectId,
     LessonTime lessonTimes
){}
