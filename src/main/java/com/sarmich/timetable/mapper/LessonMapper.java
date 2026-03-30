package com.sarmich.timetable.mapper;

import com.sarmich.timetable.domain.LessonEntity;
import com.sarmich.timetable.model.request.LessonRequest;
import com.sarmich.timetable.model.request.LessonUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InstantMapper.class)
public abstract class LessonMapper {

    public static final LessonMapper INSTANCE = Mappers.getMapper(LessonMapper.class);

    public abstract LessonEntity toEntity(LessonRequest lesson);

    public abstract LessonEntity toEntity(LessonUpdateRequest lesson);
}
