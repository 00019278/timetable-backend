package com.sarmich.timetable.mapper;

import com.sarmich.timetable.domain.ClassEntity;
import com.sarmich.timetable.model.request.ClassRequest;
import com.sarmich.timetable.model.request.ClassUpdateRequest;
import com.sarmich.timetable.model.response.ClassResponse;
import com.sarmich.timetable.model.response.GroupResponse;
import com.sarmich.timetable.model.response.RoomResponse;
import com.sarmich.timetable.model.response.TeacherResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public abstract class ClassMapper {

  public static final ClassMapper INSTANCE = Mappers.getMapper(ClassMapper.class);

  public abstract ClassEntity toEntity(ClassRequest classDto);

  public abstract ClassEntity toEntity(ClassUpdateRequest classDto);

  public abstract ClassResponse toResponse(ClassEntity classDto);

  @Mapping(source = "teacher", target = "teacher")
  @Mapping(source = "rooms", target = "rooms")
  @Mapping(source = "groups", target = "groups")
  @Mapping(source = "c.id", target = "id")
  @Mapping(source = "c.shortName", target = "shortName")
  @Mapping(source = "c.availabilities", target = "availabilities")
  @Mapping(source = "c.updatedDate", target = "updatedDate")
  @Mapping(source = "c.createdDate", target = "createdDate")
  public abstract ClassResponse toResponse(
      ClassEntity c, TeacherResponse teacher, List<RoomResponse> rooms, List<GroupResponse> groups);
}
