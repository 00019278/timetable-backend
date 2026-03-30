package com.sarmich.timetable.service;

import com.sarmich.timetable.domain.*;
import com.sarmich.timetable.exception.InvalidOperationException;
import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.mapper.*;
import com.sarmich.timetable.model.request.GroupLessonDetail;
import com.sarmich.timetable.model.request.LessonRequest;
import com.sarmich.timetable.model.response.*;
import com.sarmich.timetable.repository.*;
import com.sarmich.timetable.utils.Util;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Log4j2
public class LessonService {

  private final LessonRepository lessonRepository;
  private final ClassRepository classRepository;
  private final TeacherRepository teacherRepository;
  private final SubjectRepository subjectRepository;
  private final RoomRepository roomRepository;
  private final GroupRepository groupRepository;

  @Transactional
  public void add(Integer orgId, LessonRequest request) {
    log.debug("Adding lesson request {} for organization {}", request, orgId);
    validateLesson(request, orgId, request.classId());

    request
        .classId()
        .forEach(
            classId -> {
              LessonEntity oldLesson =
                  lessonRepository.findByUniqueKey(
                      orgId, classId, request.teacherId(), request.subjectId());

              LessonEntity entity;
              if (oldLesson != null) {
                entity = LessonMapper.INSTANCE.toEntity(oldLesson, request, classId);
              } else {
                entity = LessonMapper.INSTANCE.toEntity(request, classId);
                entity.setOrgId(orgId);
              }

              if (Util.notEmpty(request.groups())) {
                entity.setGroupDetails(request.groups());
              }

              lessonRepository.save(entity);
            });
  }

  private void validateLesson(LessonRequest request, Integer orgId, List<Integer> classIds) {
    List<ClassEntity> classEntity =
        classRepository.findByIdInAndOrgIdAndDeletedFalse(classIds, orgId);
    if (classEntity.size() != classIds.size()) {
      throw new InvalidOperationException("Class not found");
    }

    // Validate main teacher if provided (for non-group lessons)
    if (request.teacherId() != null) {
      TeacherEntity teacherEntity =
          teacherRepository.findByIdAndOrgIdAndDeletedFalse(request.teacherId(), orgId);
      Util.checkNull(ErrorCode.NOT_FOUND_ERROR_CODE, teacherEntity, TeacherEntity.class);
    }

    SubjectEntity subject =
        subjectRepository.findByIdAndOrgIdAndDeletedFalse(request.subjectId(), orgId);
    Util.checkNull(ErrorCode.NOT_FOUND_ERROR_CODE, subject, SubjectEntity.class);

    // Validate groups if present
    if (Util.notEmpty(request.groups())) {
      for (GroupLessonDetail groupDetail : request.groups()) {
        GroupEntity group = groupRepository.findById(groupDetail.groupId()).orElse(null);
        if (group == null || !group.getOrgId().equals(orgId)) {
          throw new InvalidOperationException("Group not found: " + groupDetail.groupId());
        }
        // Check if group belongs to one of the classes
        if (!classIds.contains(group.getClassId())) {
          throw new InvalidOperationException(
              "Group " + groupDetail.groupId() + " does not belong to the specified classes");
        }

        if (groupDetail.teacherId() != null) {
          TeacherEntity teacher =
              teacherRepository.findByIdAndOrgIdAndDeletedFalse(groupDetail.teacherId(), orgId);
          Util.checkNull(ErrorCode.NOT_FOUND_ERROR_CODE, teacher, TeacherEntity.class);
        }
        if (groupDetail.subjectId() != null) {
          SubjectEntity groupSubject =
              subjectRepository.findByIdAndOrgIdAndDeletedFalse(groupDetail.subjectId(), orgId);
          Util.checkNull(ErrorCode.NOT_FOUND_ERROR_CODE, groupSubject, SubjectEntity.class);
        }
      }
    }
  }

  public void update(Integer orgId, Integer id, LessonRequest request) {
    LessonEntity lesson = lessonRepository.findByIdAndOrgIdAndDeletedFalse(id, orgId);
    Util.checkNull(ErrorCode.NOT_FOUND_ERROR_CODE, lesson, LessonEntity.class);
    validateLesson(request, orgId, List.of(lesson.getClassId()));

    LessonEntity entity = LessonMapper.INSTANCE.toEntity(lesson, request, lesson.getClassId());
    if (Util.notEmpty(request.groups())) {
      entity.setGroupDetails(request.groups());
    } else {
      entity.setGroupDetails(null);
    }
    lessonRepository.save(entity);
  }

  @Transactional
  public void delete(Integer orgId, Integer id) {
    lessonRepository.deleteByIdAndOrgId(id, orgId);
  }

  public LessonResponse get(Integer orgId, Integer id) {
    LessonEntity entity = lessonRepository.findByIdAndOrgIdAndDeletedFalse(id, orgId);
    Util.checkNull(ErrorCode.NOT_FOUND_ERROR_CODE, entity, LessonEntity.class);
    return mapToResponse(entity);
  }

  public List<LessonResponse> getAllLessons(Integer orgId) {
    List<LessonEntity> entities = lessonRepository.findAllByOrgIdAndDeletedFalse(orgId);
    return mapToResponse(orgId, entities);
  }

  public PageImpl<LessonResponse> getAll(Integer orgId, Pageable page) {
    List<LessonEntity> entities = lessonRepository.findAllByOrgIdAndDeletedFalse(orgId, page);
    List<LessonResponse> responses = mapToResponse(orgId, entities);
    long totalElements = lessonRepository.countByOrgIdAndDeletedFalse(orgId);

    return new PageImpl<>(responses, page, totalElements);
  }

  private LessonResponse mapToResponse(LessonEntity entity) {
    ClassEntity classEntity = classRepository.findById(entity.getClassId()).orElse(null);
    TeacherEntity teacherEntity = teacherRepository.findById(entity.getTeacherId()).orElse(null);
    SubjectEntity subjectEntity = subjectRepository.findById(entity.getSubjectId()).orElse(null);

    List<RoomResponse> rooms = new ArrayList<>();
    if (entity.getRoomIds() != null && !entity.getRoomIds().isEmpty()) {
      Iterable<RoomEntity> roomEntities = roomRepository.findAllById(entity.getRoomIds());
      for (RoomEntity r : roomEntities) {
        rooms.add(RoomMapper.INSTANCE.toResponse(r));
      }
    }

    List<GroupLessonDetailResponse> groupDetails = new ArrayList<>();
    if (Util.notEmpty(entity.getGroupDetails())) {
      for (GroupLessonDetail detail : entity.getGroupDetails()) {
        GroupEntity group = groupRepository.findById(detail.groupId()).orElse(null);
        TeacherEntity teacher =
            detail.teacherId() != null
                ? teacherRepository.findById(detail.teacherId()).orElse(null)
                : null;
        SubjectEntity subject =
            detail.subjectId() != null
                ? subjectRepository.findById(detail.subjectId()).orElse(null)
                : null;
        List<RoomResponse> groupRooms = new ArrayList<>();
        if (Util.notEmpty(detail.roomIds())) {
          roomRepository
              .findAllById(detail.roomIds())
              .forEach(r -> groupRooms.add(RoomMapper.INSTANCE.toResponse(r)));
        }

        if (group != null) {
          groupDetails.add(
              new GroupLessonDetailResponse(
                  group.getId(),
                  teacher != null ? teacher.getId() : null,
                  subject != null ? subject.getId() : null,
                  Util.notEmpty(detail.roomIds()) ? detail.roomIds() : new ArrayList<>()));
        }
      }
    }

    ClassResponse classInfo =
        classEntity != null ? ClassMapper.INSTANCE.toResponse(classEntity) : null;
    TeacherResponse teacherInfo =
        teacherEntity != null ? TeacherMapper.INSTANCE.toResponse(teacherEntity) : null;
    SubjectResponse subjectInfo =
        subjectEntity != null ? SubjectMapper.INSTANCE.toResponse(subjectEntity) : null;

    return LessonMapper.INSTANCE.toResponse(
        entity, classInfo, teacherInfo, rooms, subjectInfo, null, groupDetails);
  }

  public LessonsWithMetadataResponse getAllLessonsWithMetadata(Integer orgId) {
    List<LessonEntity> entities = lessonRepository.findAllByOrgIdAndDeletedFalse(orgId);
    List<LessonResponse> lessonResponses = mapToResponse(orgId, entities);

    // Collect all unique IDs
    List<Integer> classIds = Util.idList(entities, LessonEntity::getClassId);
    List<Integer> teacherIds = Util.idList(entities, LessonEntity::getTeacherId);
    List<Integer> subjectIds = Util.idList(entities, LessonEntity::getSubjectId);
    List<Integer> roomIds = entities.stream()
        .flatMap(e -> Util.getNotNull(e.getRoomIds(), new ArrayList<Integer>()).stream())
        .distinct()
        .toList();

    // Also collect IDs from group details
    for (LessonEntity e : entities) {
      if (Util.notEmpty(e.getGroupDetails())) {
        for (GroupLessonDetail d : e.getGroupDetails()) {
          if (d.teacherId() != null && !teacherIds.contains(d.teacherId())) {
            teacherIds = new ArrayList<>(teacherIds);
            teacherIds.add(d.teacherId());
          }
          if (d.subjectId() != null && !subjectIds.contains(d.subjectId())) {
            subjectIds = new ArrayList<>(subjectIds);
            subjectIds.add(d.subjectId());
          }
          if (Util.notEmpty(d.roomIds())) {
            for (Integer rid : d.roomIds()) {
              if (!roomIds.contains(rid)) {
                roomIds = new ArrayList<>(roomIds);
                roomIds.add(rid);
              }
            }
          }
        }
      }
    }

    // Fetch all related entities
    List<ClassResponse> classes = classRepository
        .findAllByOrgIdAndIdInAndDeletedFalse(orgId, classIds)
        .stream()
        .map(ClassMapper.INSTANCE::toResponse)
        .toList();

    List<TeacherResponse> teachers = teacherRepository
        .findAllByOrgIdAndIdInAndDeletedFalse(orgId, teacherIds)
        .stream()
        .map(TeacherMapper.INSTANCE::toResponse)
        .toList();

    List<SubjectResponse> subjects = subjectRepository
        .findAllByOrgIdAndIdInAndDeletedFalse(orgId, subjectIds)
        .stream()
        .map(SubjectMapper.INSTANCE::toResponse)
        .toList();

    List<RoomResponse> rooms = roomRepository
        .findAllByIdInAndDeletedFalse(roomIds)
        .stream()
        .map(RoomMapper.INSTANCE::toResponse)
        .toList();

    return new LessonsWithMetadataResponse(lessonResponses, classes, teachers, rooms, subjects);
  }

  private List<LessonResponse> mapToResponse(Integer orgId, List<LessonEntity> entity) {
    List<ClassResponse> classes =
        classRepository
            .findAllByOrgIdAndIdInAndDeletedFalse(
                orgId, Util.idList(entity, LessonEntity::getClassId))
            .stream()
            .map(ClassMapper.INSTANCE::toResponse)
            .toList();
    List<TeacherResponse> teachers =
        teacherRepository
            .findAllByOrgIdAndIdInAndDeletedFalse(
                orgId, Util.idList(entity, LessonEntity::getTeacherId))
            .stream()
            .map(TeacherMapper.INSTANCE::toResponse)
            .toList();
    List<SubjectResponse> subjects =
        subjectRepository
            .findAllByOrgIdAndIdInAndDeletedFalse(
                orgId, Util.idList(entity, LessonEntity::getSubjectId))
            .stream()
            .map(SubjectMapper.INSTANCE::toResponse)
            .toList();
    List<Integer> rooms =
        entity.stream()
            .flatMap(e -> Util.getNotNull(e.getRoomIds(), new ArrayList<Integer>()).stream())
            .toList();
    List<RoomResponse> roomResponses =
        roomRepository.findAllByIdInAndDeletedFalse(rooms).stream()
            .map(RoomMapper.INSTANCE::toResponse)
            .toList();

    HashMap<Integer, RoomResponse> roomMap = Util.mapById(roomResponses, RoomResponse::id);
    HashMap<Integer, ClassResponse> classMap = Util.mapById(classes, ClassResponse::id);
    HashMap<Integer, TeacherResponse> teacherMap = Util.mapById(teachers, TeacherResponse::id);
    HashMap<Integer, SubjectResponse> subjectMap = Util.mapById(subjects, SubjectResponse::id);

    List<Integer> allGroupIds = new ArrayList<>();
    List<Integer> allGroupTeacherIds = new ArrayList<>();
    List<Integer> allGroupSubjectIds = new ArrayList<>();
    List<Integer> allGroupRoomIds = new ArrayList<>();

    for (LessonEntity e : entity) {
      if (Util.notEmpty(e.getGroupDetails())) {
        for (GroupLessonDetail d : e.getGroupDetails()) {
          if (d.groupId() != null) allGroupIds.add(d.groupId());
          if (d.teacherId() != null) allGroupTeacherIds.add(d.teacherId());
          if (d.subjectId() != null) allGroupSubjectIds.add(d.subjectId());
          if (Util.notEmpty(d.roomIds())) allGroupRoomIds.addAll(d.roomIds());
        }
      }
    }

    HashMap<Integer, GroupResponse> groupMap = new HashMap<>();
    if (!allGroupIds.isEmpty()) {
      groupRepository
          .findAllById(allGroupIds)
          .forEach(g -> groupMap.put(g.getId(), new GroupResponse(g.getId(), g.getName())));
    }

    HashMap<Integer, TeacherResponse> groupTeacherMap = new HashMap<>();
    if (!allGroupTeacherIds.isEmpty()) {
      teacherRepository
          .findAllById(allGroupTeacherIds)
          .forEach(t -> groupTeacherMap.put(t.getId(), TeacherMapper.INSTANCE.toResponse(t)));
    }

    HashMap<Integer, SubjectResponse> groupSubjectMap = new HashMap<>();
    if (!allGroupSubjectIds.isEmpty()) {
      subjectRepository
          .findAllById(allGroupSubjectIds)
          .forEach(s -> groupSubjectMap.put(s.getId(), SubjectMapper.INSTANCE.toResponse(s)));
    }

    HashMap<Integer, RoomResponse> groupRoomMap = new HashMap<>();
    if (!allGroupRoomIds.isEmpty()) {
      roomRepository
          .findAllById(allGroupRoomIds)
          .forEach(r -> groupRoomMap.put(r.getId(), RoomMapper.INSTANCE.toResponse(r)));
    }

    return entity.stream()
        .map(
            e -> {
              List<GroupLessonDetailResponse> groupDetails = new ArrayList<>();
              if (Util.notEmpty(e.getGroupDetails())) {
                for (GroupLessonDetail d : e.getGroupDetails()) {
                  GroupResponse g = groupMap.get(d.groupId());
                  if (g != null) {
                    List<Integer> grRoomIds = new ArrayList<>();
                    if (Util.notEmpty(d.roomIds())) {
                         grRoomIds = d.roomIds();
                    }
                    groupDetails.add(
                        new GroupLessonDetailResponse(
                            g.id(),
                            d.teacherId(),
                            d.subjectId(),
                            grRoomIds));
                  }
                }
              }

              return LessonMapper.INSTANCE.toResponse(
                  e,
                  classMap.get(e.getClassId()),
                  teacherMap.get(e.getTeacherId()),
                  e.getRoomIds() == null
                      ? new ArrayList<>()
                      : e.getRoomIds().stream().map(roomMap::get).toList(),
                  subjectMap.get(e.getSubjectId()),
                  null,
                  groupDetails);
            })
        .toList();
  }
}
