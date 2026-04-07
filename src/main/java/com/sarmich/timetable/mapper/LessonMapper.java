package com.sarmich.timetable.mapper;

import com.sarmich.timetable.domain.LessonEntity;
import com.sarmich.timetable.model.request.LessonRequest;
import com.sarmich.timetable.model.response.*;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = InstantMapper.class)
public abstract class LessonMapper {

  public static final LessonMapper INSTANCE = Mappers.getMapper(LessonMapper.class);

  @Mapping(target = "classId", source = "classId")
  public abstract LessonEntity toEntity(LessonRequest lesson, Integer classId);

  @Mapping(target = "classId", source = "classId")
  public abstract LessonEntity toEntity(
      @MappingTarget LessonEntity entity, LessonRequest lesson, Integer classId);

  // Manual mapping might be needed if MapStruct cannot figure out ID mapping automatically
  // But here we are passing Objects to toResponse, and LessonResponse expects IDs.
  // So we need to map those objects to IDs.

  @Mapping(target = "id", source = "entity.id")
  @Mapping(target = "createdDate", source = "entity.createdDate")
  @Mapping(target = "updatedDate", source = "entity.updatedDate")
  // Map Objects to IDs
  @Mapping(target = "teacherId", source = "teacher.id")
  @Mapping(target = "classId", source = "classInfo.id")
  @Mapping(target = "subjectId", source = "subject.id")
  @Mapping(target = "groupId", source = "group.id")
  // List<RoomResponse> rooms -> List<Integer> roomIds
  @Mapping(
      target = "roomIds",
      expression =
          "java(rooms != null ? rooms.stream().map(com.sarmich.timetable.model.response.RoomResponse::id).toList() : null)")
  @Mapping(target = "groupDetails", source = "groupDetails")
  public abstract LessonResponse toResponse(
      LessonEntity entity,
      ClassResponse classInfo,
      TeacherResponse teacher,
      List<RoomResponse> rooms,
      SubjectResponse subject,
      GroupResponse group,
      List<GroupLessonDetailResponse> groupDetails);
}
