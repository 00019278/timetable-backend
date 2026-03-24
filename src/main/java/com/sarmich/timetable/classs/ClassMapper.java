package com.sarmich.timetable.classs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarmich.timetable.mapper.InstantMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InstantMapper.class)
public abstract class ClassMapper {

    public static final ClassMapper INSTANCE = Mappers.getMapper(ClassMapper.class);

    @Mapping(target = "days", ignore = true)
    public abstract ClassEntity toEntity(ClassRequest classDto);

    public ClassEntity toEntity(ClassRequest classDto, ObjectMapper objectMapper) {
        ClassEntity entity = toEntity(classDto);
        entity.setDays(objectMapper.convertValue(classDto.getDays(), new TypeReference<Map<String, Object>>() {}));
        return entity;
    }

    @Mapping(target = "days", ignore = true)
    public abstract ClassEntity toEntity(ClassUpdateRequest classDto);

    public ClassEntity toEntity(ClassUpdateRequest classDto, ObjectMapper objectMapper) {
        ClassEntity entity = toEntity(classDto);
        entity.setDays(objectMapper.convertValue(classDto.getDays(), new TypeReference<Map<String, Object>>() {}));
        return entity;
    }

    @Mapping(target = "days", ignore = true)
    public abstract ClassResponse toResponse(ClassEntity classDto);

    public ClassResponse toResponse(ClassEntity classDto, ObjectMapper objectMapper) {
        ClassResponse res = toResponse(classDto);
        res.setDays(objectMapper.convertValue(classDto.getDays(), new TypeReference<List<DayOfWeek>>() {}));
        return res;
    }
}
