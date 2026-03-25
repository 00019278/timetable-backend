package com.sarmich.timetable.model.response;

public record SubjectResponse (

     Long id,
     String shortName,
     String name,
     Integer priority
){}