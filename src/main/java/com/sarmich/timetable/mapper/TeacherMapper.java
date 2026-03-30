package com.sarmich.timetable.mapper;

import com.sarmich.timetable.domain.TeacherEntity;
import com.sarmich.timetable.model.request.TeacherRequest;
import com.sarmich.timetable.model.request.TeacherUpdateRequest;
import com.sarmich.timetable.model.response.SubjectResponse;
import com.sarmich.timetable.model.response.TeacherResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InstantMapper.class)
public abstract class TeacherMapper {
  public static final TeacherMapper INSTANCE = Mappers.getMapper(TeacherMapper.class);

  public abstract TeacherEntity toEntity(TeacherRequest teacher);

  public abstract TeacherEntity updateEntityFromDto(
      TeacherUpdateRequest dto, @MappingTarget TeacherEntity entity);

  @Mapping(target = "subjects", ignore = true)
  public abstract TeacherResponse toResponse(TeacherEntity teacher);

  @Mapping(target = "subjects", source = "subjects")
  public abstract TeacherResponse toResponse(TeacherEntity teacher, List<SubjectResponse> subjects);
}
