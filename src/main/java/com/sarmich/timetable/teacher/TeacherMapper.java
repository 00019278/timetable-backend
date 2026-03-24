package com.sarmich.timetable.teacher;

import com.sarmich.timetable.mapper.InstantMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InstantMapper.class)
public abstract class TeacherMapper {
    public static final TeacherMapper INSTANCE = Mappers.getMapper(TeacherMapper.class);

    public abstract TeacherEntity toEntity(TeacherRequest teacher);
    public abstract TeacherEntity toEntity(TeacherUpdateRequest teacher);
    public abstract TeacherResponse toResponse(TeacherEntity teacher);
}
