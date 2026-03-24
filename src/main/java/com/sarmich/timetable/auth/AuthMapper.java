package com.sarmich.timetable.auth;

import com.sarmich.timetable.mapper.InstantMapper;
import com.sarmich.timetable.utils.ProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InstantMapper.class)
public abstract class AuthMapper {

    public static final AuthMapper INSTANCE = Mappers.getMapper(AuthMapper.class);

    public abstract ProfileEntity toEntity(SignUpRequest req);
}
