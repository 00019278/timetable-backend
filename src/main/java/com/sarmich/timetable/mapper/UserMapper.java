package com.sarmich.timetable.mapper;

import com.sarmich.timetable.domain.UserEntity;
import com.sarmich.timetable.model.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InstantMapper.class)
public abstract class UserMapper {

  public static final UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  public abstract UserResponse toResponse(UserEntity entity);
}
