package com.sarmich.timetable.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarmich.timetable.classs.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.DayOfWeek;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InstantMapper.class)
public abstract class ClassMapper {

    public static final ClassMapper INSTANCE = Mappers.getMapper(ClassMapper.class);

    @Mapping(target = "days", ignore = true)
    public abstract ClassEntity toEntity(ClassRequest classDto);

    public ClassEntity toEntity(ClassRequest classDto, ObjectMapper objectMapper) {
        ClassEntity entity = toEntity(classDto);
        entity.setDays(objectMapper.convertValue(classDto.days(), new TypeReference<>() {
        }));
        return entity;
    }

    @Mapping(target = "days", ignore = true)
    public abstract ClassEntity toEntity(ClassUpdateRequest classDto);

    public ClassEntity toEntity(ClassUpdateRequest classDto, ObjectMapper objectMapper) {
        ClassEntity entity = toEntity(classDto);
        entity.setDays(objectMapper.convertValue(classDto.days(), new TypeReference<>() {
        }));
        return entity;
    }

    @Mapping(target = "days", source = "days")
    public abstract ClassResponse toResponse(ClassEntity classDto, Days days);

    public ClassResponse toResponse(ClassEntity entity, ObjectMapper objectMapper) {
        return toResponse(entity, objectMapper.convertValue(entity.getDays(), Days.class));
    }
}
