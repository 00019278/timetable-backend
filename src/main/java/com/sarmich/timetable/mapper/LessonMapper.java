package com.sarmich.timetable.mapper;

import com.sarmich.timetable.lesson.*;
import com.sarmich.timetable.subject.SubjectEntity;
import com.sarmich.timetable.subject.SubjectRequest;
import com.sarmich.timetable.subject.SubjectResponse;
import com.sarmich.timetable.subject.SubjectUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InstantMapper.class)
public abstract class LessonMapper {

    public static final LessonMapper INSTANCE = Mappers.getMapper(LessonMapper.class);


    public abstract LessonEntity toEntity(LessonRequest lesson);


    public abstract LessonEntity toEntity(LessonUpdateRequest subject);

    @Mapping(target = "info", source = "lessonInfo")
    public abstract LessonResponse toResponse(LessonEntity subject, LessonInfo lessonInfo);
}
