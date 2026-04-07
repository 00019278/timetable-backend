package com.sarmich.timetable.model.response;

import java.util.List;

/**
 * Response containing all lessons along with related metadata (classes, teachers, rooms, subjects).
 * This reduces JSON size by avoiding duplicate data - each entity is sent only once, and lessons
 * reference them by ID.
 */
public record LessonsWithMetadataResponse(
    List<LessonResponse> lessons,
    List<ClassResponse> classes,
    List<TeacherResponse> teachers,
    List<RoomResponse> rooms,
    List<SubjectResponse> subjects) {}
