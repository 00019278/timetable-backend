package com.sarmich.timetable.mapper;

import com.sarmich.timetable.domain.GroupEntity;
import com.sarmich.timetable.model.request.GroupRequest;
import com.sarmich.timetable.model.request.GroupUpdateRequest;
import com.sarmich.timetable.model.response.GroupResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GroupMapper {

  GroupMapper INSTANCE = Mappers.getMapper(GroupMapper.class);

  GroupEntity toEntity(GroupRequest request);

  GroupEntity toEntity(GroupUpdateRequest request);

  GroupResponse toResponse(GroupEntity entity);
}
