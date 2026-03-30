package com.sarmich.timetable.service;

import com.sarmich.timetable.domain.ClassEntity;
import com.sarmich.timetable.domain.ClassRoomEntity;
import com.sarmich.timetable.domain.GroupEntity;
import com.sarmich.timetable.domain.TeacherEntity;
import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.mapper.ClassMapper;
import com.sarmich.timetable.mapper.GroupMapper;
import com.sarmich.timetable.mapper.RoomMapper;
import com.sarmich.timetable.mapper.TeacherMapper;
import com.sarmich.timetable.model.request.ApplyToOthersRequest;
import com.sarmich.timetable.model.request.ClassRequest;
import com.sarmich.timetable.model.request.ClassUpdateRequest;
import com.sarmich.timetable.model.request.GroupRequest;
import com.sarmich.timetable.model.request.GroupUpdateRequest;
import com.sarmich.timetable.model.response.ClassResponse;
import com.sarmich.timetable.model.response.GroupResponse;
import com.sarmich.timetable.model.response.RoomResponse;
import com.sarmich.timetable.model.response.TeacherResponse;
import com.sarmich.timetable.repository.ClassRepository;
import com.sarmich.timetable.repository.ClassRoomRepository;
import com.sarmich.timetable.repository.GroupRepository;
import com.sarmich.timetable.repository.RoomRepository;
import com.sarmich.timetable.repository.TeacherRepository;
import com.sarmich.timetable.utils.Util;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@AllArgsConstructor
public class ClassService {
  private final ClassRepository classRepository;
  private final TeacherRepository teacherRepository;
  private final ClassRoomRepository classRoomRepository;
  private final RoomRepository roomRepository;
  private final GroupRepository groupRepository;

  @Transactional
  public void add(Integer orgId, ClassRequest request) {
    log.debug("Adding class request {} {}", orgId, request);
    validateTeacher(orgId, request.teacherId());

    ClassEntity entity = ClassMapper.INSTANCE.toEntity(request);
    entity.setOrgId(orgId);
    ClassEntity savedEntity = classRepository.save(entity);

    updateRoomsForClass(orgId, savedEntity.getId(), request.rooms(), Collections.emptySet());
    updateGroupsForClass(
        orgId,
        savedEntity.getId(),
        request.groups(),
        Collections.emptyList(),
        Collections.emptySet());
  }

  @Transactional
  public void update(Integer orgId, Integer id, ClassUpdateRequest request) {
    log.debug("Updating class request {} {}", orgId, request);
    ClassEntity entity = classRepository.findByIdAndOrgIdAndDeletedFalse(id, orgId);
    Util.checkNull(entity, ClassEntity.class);
    validateTeacher(orgId, request.teacherId());

    // update mutable fields
    entity.setName(request.name());
    entity.setShortName(request.shortName());
    entity.setAvailabilities(request.availabilities());
    entity.setTeacherId(request.teacherId());
    entity.setUpdatedDate(Instant.now());
    updateRoomsForClass(orgId, entity.getId(), request.rooms(), request.deletedRooms());
    updateGroupsForClass(
        orgId,
        entity.getId(),
        request.newGroups(),
        request.updatedGroups(),
        request.deletedGroupIds());
    classRepository.saveAndFlush(entity);
  }

  @Transactional
  public void delete(Integer orgId, Integer id) {
    log.debug("Delete class with id: {}", id);
    classRepository.updateDeleted(id, orgId);
    classRoomRepository.deleteAllByClassIdAndOrgId(id, orgId);
    groupRepository.deleteAllByClassIdAndOrgId(id, orgId);
  }

  public PageImpl<ClassResponse> getAll(
      Integer orgId, Boolean withRooms, Boolean withTeacher, Pageable pageable) {
    List<ClassEntity> classEntities =
        classRepository.findAllByOrgIdAndDeletedFalse(orgId, pageable);
    List<ClassResponse> classes =
        fetchClassWithRoomAndTeacherAndGroups(classEntities, orgId, withRooms, withTeacher);
    return new PageImpl<>(classes, pageable, classRepository.countByOrgIdAndDeletedFalse(orgId));
  }

  public List<ClassResponse> getAllClass(int orgId, Boolean withRooms, Boolean withTeacher) {
    List<ClassEntity> classes = classRepository.findAllByOrgIdAndDeletedFalse(orgId);
    return fetchClassWithRoomAndTeacherAndGroups(classes, orgId, withRooms, withTeacher);
  }

  private void validateTeacher(Integer orgId, Integer teacherId) {
    if (teacherId != null) {
      TeacherEntity teacher = teacherRepository.findByIdAndOrgIdAndDeletedFalse(teacherId, orgId);
      Util.checkNull(ErrorCode.NOT_FOUND_ERROR_CODE, teacher, TeacherEntity.class);
    }
  }

  private void updateRoomsForClass(
      Integer orgId, Integer classId, Set<Integer> newRoomIds, Set<Integer> deletedRoomIds) {
    if (Util.notEmpty(deletedRoomIds)) {
      classRoomRepository.deleteAllByClassIdAndRoomIdIn(classId, deletedRoomIds);
    }
    if (Util.notEmpty(newRoomIds)) {
      List<ClassRoomEntity> classRooms =
          newRoomIds.stream()
              .map(
                  roomId -> {
                    ClassRoomEntity classRoom = new ClassRoomEntity();
                    classRoom.setOrgId(orgId);
                    classRoom.setRoomId(roomId);
                    classRoom.setClassId(classId);
                    return classRoom;
                  })
              .toList();
      classRoomRepository.saveAll(classRooms);
    }
  }

  private void updateGroupsForClass(
      Integer orgId,
      Integer classId,
      List<GroupRequest> newGroups,
      List<GroupUpdateRequest> updatedGroups,
      Set<Integer> deletedGroupIds) {
    if (Util.notEmpty(deletedGroupIds)) {
      deletedGroupIds.forEach(groupId -> groupRepository.updateDeleted(groupId, orgId));
    }

    if (Util.notEmpty(newGroups)) {
      List<GroupEntity> groups =
          newGroups.stream()
              .map(
                  groupRequest -> {
                    GroupEntity group = GroupMapper.INSTANCE.toEntity(groupRequest);
                    group.setOrgId(orgId);
                    group.setClassId(classId);
                    return group;
                  })
              .toList();
      groupRepository.saveAll(groups);
    }

    if (Util.notEmpty(updatedGroups)) {
      updatedGroups.forEach(
          groupUpdate ->
              groupRepository
                  .findById(groupUpdate.id())
                  .ifPresent(
                      group -> {
                        if (group.getOrgId().equals(orgId) && group.getClassId().equals(classId)) {
                          group.setName(groupUpdate.name());
                          group.setUpdatedDate(Instant.now());
                          groupRepository.save(group);
                        }
                      }));
    }
  }

  private List<ClassResponse> fetchClassWithRoomAndTeacherAndGroups(
      List<ClassEntity> classEntities, Integer orgId, Boolean withRooms, Boolean withTeacher) {
    if (classEntities.isEmpty()) {
      return Collections.emptyList();
    }

    Map<Integer, TeacherResponse> teacherMap = fetchTeachers(orgId, classEntities, withTeacher);
    Map<Integer, List<RoomResponse>> roomMap = fetchRooms(classEntities, withRooms);
    Map<Integer, List<GroupResponse>> groupMap = fetchGroups(classEntities, orgId);

    return classEntities.stream()
        .map(
            c ->
                ClassMapper.INSTANCE.toResponse(
                    c,
                    teacherMap.get(c.getTeacherId()),
                    roomMap.getOrDefault(c.getId(), Collections.emptyList()),
                    groupMap.getOrDefault(c.getId(), Collections.emptyList())))
        .toList();
  }

  private Map<Integer, TeacherResponse> fetchTeachers(
      Integer orgId, List<ClassEntity> classEntities, Boolean withTeacher) {
    if (Boolean.FALSE.equals(withTeacher)) {
      return Collections.emptyMap();
    }
    List<Integer> teacherIds =
        classEntities.stream()
            .map(ClassEntity::getTeacherId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

    if (teacherIds.isEmpty()) {
      return Collections.emptyMap();
    }

    return teacherRepository.findAllByOrgIdAndIdInAndDeletedFalse(orgId, teacherIds).stream()
        .map(TeacherMapper.INSTANCE::toResponse)
        .collect(Collectors.toMap(TeacherResponse::id, Function.identity()));
  }

  private Map<Integer, List<RoomResponse>> fetchRooms(
      List<ClassEntity> classEntities, Boolean withRooms) {
    if (Boolean.FALSE.equals(withRooms)) {
      return Collections.emptyMap();
    }
    List<Integer> classIds =
        classEntities.stream().map(ClassEntity::getId).filter(Objects::nonNull).toList();

    if (classIds.isEmpty()) {
      return Collections.emptyMap();
    }

    List<ClassRoomEntity> classRoomAssociations = classRoomRepository.findAllByClassIdIn(classIds);
    List<Integer> roomIds =
        classRoomAssociations.stream().map(ClassRoomEntity::getRoomId).distinct().toList();

    if (roomIds.isEmpty()) {
      return Collections.emptyMap();
    }

    Map<Integer, RoomResponse> roomsById =
        roomRepository.findAllByIdInAndDeletedFalse(roomIds).stream()
            .map(RoomMapper.INSTANCE::toResponse)
            .collect(Collectors.toMap(RoomResponse::id, Function.identity()));

    return classRoomAssociations.stream()
        .collect(
            Collectors.groupingBy(
                ClassRoomEntity::getClassId,
                Collectors.mapping(cr -> roomsById.get(cr.getRoomId()), Collectors.toList())));
  }

  private Map<Integer, List<GroupResponse>> fetchGroups(
      List<ClassEntity> classEntities, Integer orgId) {
    List<Integer> classIds =
        classEntities.stream().map(ClassEntity::getId).filter(Objects::nonNull).toList();

    if (classIds.isEmpty()) {
      return Collections.emptyMap();
    }

    List<GroupEntity> groups =
        groupRepository.findAllByClassIdInAndOrgIdAndDeletedFalse(classIds, orgId);

    return groups.stream()
        .collect(
            Collectors.groupingBy(
                GroupEntity::getClassId,
                Collectors.mapping(GroupMapper.INSTANCE::toResponse, Collectors.toList())));
  }

  public ClassResponse findById(Integer orgId, Integer id) {
    log.debug("Find class by id");
    ClassEntity classEntity = classRepository.findByIdAndOrgIdAndDeletedFalse(id, orgId);
    if (classEntity == null) return null;
    return fetchClassWithRoomAndTeacherAndGroups(
            Collections.singletonList(classEntity), orgId, true, true)
        .getFirst();
  }

  @Transactional
  public void applyToOthers(Integer orgId, ApplyToOthersRequest dto) {
    log.debug("Apply to others request {} {}", orgId, dto);
    classRepository.applyToOthers(orgId, dto.applyTo(), dto.timeOff());
  }
}
