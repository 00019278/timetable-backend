package com.sarmich.timetable.subject;

public record SubjectResponse (

     Long id,
     String shortName,
     String name,
     Integer priority
){}