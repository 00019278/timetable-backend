package com.sarmich.timetable.mapper;

import com.sarmich.timetable.model.request.SignUpRequest;
import com.sarmich.timetable.domain.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InstantMapper.class)
public abstract class AuthMapper {

    public static final AuthMapper INSTANCE = Mappers.getMapper(AuthMapper.class);

    public abstract UserEntity toEntity(SignUpRequest req);
}
