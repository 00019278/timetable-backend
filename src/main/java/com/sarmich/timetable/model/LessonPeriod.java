package com.sarmich.timetable.model;

public record LessonPeriod(
    String name, String startTime, String endTime, Integer duration, Boolean isBreak) {}
