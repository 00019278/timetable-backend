package com.sarmich.timetable.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarmich.timetable.teacher.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InstantMapper.class)
public abstract class TeacherMapper {
    public static final TeacherMapper INSTANCE = Mappers.getMapper(TeacherMapper.class);

    @Mapping(target = "lessonTimes", ignore = true)
    public abstract TeacherEntity toEntity(TeacherRequest teacher);
    @Mapping(target = "lessonTimes", ignore = true)
    public abstract TeacherEntity toEntity(TeacherUpdateRequest teacher);

    @Mapping(target = "lessonTimes", source = "lessonTimes")
    public abstract TeacherResponse toResponse(TeacherEntity teacher, LessonTime lessonTimes);
    @Mapping(target = "lessonTimes", ignore = true)
    public abstract TeacherResponse toResponse(TeacherEntity teacher);

    public TeacherResponse toResponse(TeacherEntity teacher, ObjectMapper objectMapper) {
        return toResponse(teacher, objectMapper.convertValue(teacher.getLessonTimes(), LessonTime.class));
    }
}
