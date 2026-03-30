package com.sarmich.timetable.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarmich.timetable.domain.ClassEntity;
import com.sarmich.timetable.model.Days;
import com.sarmich.timetable.model.request.ClassRequest;
import com.sarmich.timetable.model.request.ClassUpdateRequest;
import com.sarmich.timetable.model.response.ClassResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InstantMapper.class)
public abstract class ClassMapper {

    public static final ClassMapper INSTANCE = Mappers.getMapper(ClassMapper.class);

    public abstract ClassEntity toEntity(ClassRequest classDto);

    public abstract ClassEntity toEntity(ClassUpdateRequest classDto);

    public abstract ClassResponse toResponse(ClassEntity classDto);
}
