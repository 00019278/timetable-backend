package com.sarmich.timetable.mapper;

import com.sarmich.timetable.domain.RoomEntity;
import com.sarmich.timetable.model.request.RoomRequest;
import com.sarmich.timetable.model.response.BuildingResponse;
import com.sarmich.timetable.model.response.RoomResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public abstract class RoomMapper {

  public static final RoomMapper INSTANCE = Mappers.getMapper(RoomMapper.class);

  public abstract RoomEntity toEntity(RoomRequest classDto);

  public abstract RoomResponse toResponse(RoomEntity classDto);

  @Mapping(target = "building", source = "building")
  @Mapping(target = "id", source = "room.id")
  @Mapping(target = "name", source = "room.name")
  public abstract RoomResponse toResponse(RoomEntity room, BuildingResponse building);
}
