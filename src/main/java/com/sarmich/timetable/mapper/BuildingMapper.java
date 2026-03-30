package com.sarmich.timetable.mapper;

import com.sarmich.timetable.domain.BuildingEntity;
import com.sarmich.timetable.model.request.BuildingRequest;
import com.sarmich.timetable.model.response.BuildingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BuildingMapper {

  BuildingMapper INSTANCE = Mappers.getMapper(BuildingMapper.class);

  BuildingEntity toEntity(BuildingRequest request);

  BuildingResponse toResponse(BuildingEntity entity);
}
