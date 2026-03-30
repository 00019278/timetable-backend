package com.sarmich.timetable.service;

import com.sarmich.timetable.domain.SubjectEntity;
import com.sarmich.timetable.domain.TeacherEntity;
import com.sarmich.timetable.domain.TeacherSubjectEntity;
import com.sarmich.timetable.exception.NotFoundException;
import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.mapper.SubjectMapper;
import com.sarmich.timetable.mapper.TeacherMapper;
import com.sarmich.timetable.model.request.TeacherRequest;
import com.sarmich.timetable.model.request.TeacherUpdateRequest;
import com.sarmich.timetable.model.response.SubjectResponse;
import com.sarmich.timetable.model.response.TeacherResponse;
import com.sarmich.timetable.repository.SubjectRepository;
import com.sarmich.timetable.repository.TeacherRepository;
import com.sarmich.timetable.repository.TeacherSubjectRepository;
import com.sarmich.timetable.utils.Util;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TeacherService {
  private final TeacherRepository teacherRepository;
  private final SubjectRepository subjectRepository;
  private final TeacherSubjectRepository teacherSubjectRepository;

  @Transactional
  public void create(Integer orgId, TeacherRequest dto) {
    TeacherEntity entity = TeacherMapper.INSTANCE.toEntity(dto);
    entity.setOrgId(orgId);
    entity = teacherRepository.save(entity);
    saveTeacherSubjects(orgId, dto.subjects(), entity.getId());
    toTeacherResponse(entity, Collections.emptyList());
  }

  @Transactional
  public void update(Integer orgId, Integer teacherId, TeacherUpdateRequest dto) {
    TeacherEntity entity = teacherRepository.findByIdAndOrgIdAndDeletedFalse(teacherId, orgId);
    Util.checkNull(entity, TeacherEntity.class);
    if (Util.notEmpty(dto.deletedSubjects())) {
      teacherSubjectRepository.deleteByTeacherIdAndSubjectIdIn(orgId, dto.deletedSubjects());
    }
    saveTeacherSubjects(orgId, dto.subjects(), entity.getId());
    entity = TeacherMapper.INSTANCE.updateEntityFromDto(dto, entity);
    teacherRepository.save(entity);
  }

  private TeacherResponse toResponse(TeacherEntity entity) {
    List<TeacherSubjectEntity> teacherSubjects =
        teacherSubjectRepository.findAllByTeacherIdAndDeletedFalse(entity.getId());
    List<Integer> subjectIds =
        teacherSubjects.stream().map(TeacherSubjectEntity::getSubjectId).toList();
    List<SubjectEntity> subjects =
        subjectRepository.findAllByOrgIdAndIdInAndDeletedFalse(entity.getOrgId(), subjectIds);

    return toTeacherResponse(entity, subjects);
  }

  private void saveTeacherSubjects(Integer orgId, List<Integer> subjectIds, Integer teacherId) {
    if (Util.notEmpty(subjectIds)) {
      List<SubjectEntity> subjects =
          subjectRepository.findAllByOrgIdAndIdInAndDeletedFalse(orgId, subjectIds);
      for (SubjectEntity subject : subjects) {
        TeacherSubjectEntity teacherSubject = new TeacherSubjectEntity();
        teacherSubject.setOrgId(orgId);
        teacherSubject.setTeacherId(teacherId);
        teacherSubject.setSubjectId(subject.getId());
        teacherSubjectRepository.save(teacherSubject);
      }
    }
  }

  public void delete(Integer orgId, Integer id) {
    TeacherEntity teacher = teacherRepository.findByIdAndOrgIdAndDeletedFalse(id, orgId);
    if (teacher == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_ERROR_CODE, "Teacher not found");
    }
    teacher.setDeleted(true);
    teacherRepository.save(teacher);
  }

  public TeacherResponse get(Integer orgId, Integer id) {
    TeacherEntity entity = teacherRepository.findByIdAndOrgIdAndDeletedFalse(id, orgId);
    if (entity == null) {
      throw new NotFoundException(ErrorCode.NOT_FOUND_ERROR_CODE, "Teacher not found");
    }
    return toResponse(entity);
  }

  public List<TeacherResponse> findAll(Integer orgId, Boolean withSubjects) {
    List<TeacherEntity> teachers = teacherRepository.findAllByOrgIdAndDeletedFalse(orgId);
    if (Boolean.TRUE.equals(withSubjects)) {
      return toTeacherResponseList(orgId, teachers);
    } else {
      return teachers.stream().map(TeacherMapper.INSTANCE::toResponse).toList();
    }
  }

  public Page<TeacherResponse> findAll(Integer orgId, Boolean withSubjects, Pageable pageable) {
    Page<TeacherEntity> teacherPage =
        teacherRepository.findAllByOrgIdAndDeletedFalse(orgId, pageable);
    if (Boolean.TRUE.equals(withSubjects)) {
      List<TeacherResponse> responses = toTeacherResponseList(orgId, teacherPage.getContent());
      return new PageImpl<>(responses, pageable, teacherPage.getTotalElements());
    } else {
      return teacherPage.map(TeacherMapper.INSTANCE::toResponse);
    }
  }

  private List<TeacherResponse> toTeacherResponseList(Integer orgId, List<TeacherEntity> teachers) {
    if (teachers.isEmpty()) {
      return Collections.emptyList();
    }
    List<Integer> teacherIds = teachers.stream().map(TeacherEntity::getId).toList();
    List<TeacherSubjectEntity> teacherSubjects =
        teacherSubjectRepository.findAllByTeacherIdInAndDeletedFalse(teacherIds);
    List<Integer> subjectIds =
        teacherSubjects.stream().map(TeacherSubjectEntity::getSubjectId).toList();
    List<SubjectEntity> subjects =
        subjectRepository.findAllByOrgIdAndIdInAndDeletedFalse(orgId, subjectIds);

    Map<Integer, List<SubjectEntity>> teacherSubjectsMap =
        teacherSubjects.stream()
            .collect(
                Collectors.groupingBy(
                    TeacherSubjectEntity::getTeacherId,
                    Collectors.mapping(
                        ts ->
                            subjects.stream()
                                .filter(s -> s.getId().equals(ts.getSubjectId()))
                                .findFirst()
                                .orElse(null),
                        Collectors.toList())));

    return teachers.stream()
        .map(teacher -> toTeacherResponse(teacher, teacherSubjectsMap.get(teacher.getId())))
        .toList();
  }

  private TeacherResponse toTeacherResponse(TeacherEntity teacher, List<SubjectEntity> subjects) {
    List<SubjectResponse> subjectResponses =
        subjects == null ? Collections.emptyList() : SubjectMapper.INSTANCE.toResponse(subjects);
    return TeacherMapper.INSTANCE.toResponse(teacher, subjectResponses);
  }
}
