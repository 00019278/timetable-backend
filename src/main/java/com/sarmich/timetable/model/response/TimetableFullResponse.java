package com.sarmich.timetable.model.response;

import com.sarmich.timetable.model.TimetableDataResponse;
import java.util.List;

public record TimetableFullResponse(
    List<TimetableDataResponse> timetableData,
    List<ClassResponse> classes,
    List<TeacherResponse> teachers,
    List<SubjectResponse> subjects,
    List<RoomResponse> rooms,
    List<GroupResponse> groups) {}
