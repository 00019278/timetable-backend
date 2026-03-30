package com.sarmich.timetable.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarmich.timetable.domain.TeacherEntity;
import com.sarmich.timetable.exception.NotFoundException;
import com.sarmich.timetable.mapper.TeacherMapper;
import com.sarmich.timetable.model.request.TeacherRequest;
import com.sarmich.timetable.model.response.TeacherResponse;
import com.sarmich.timetable.repository.TeacherRepository;
import com.sarmich.timetable.domain.SubjectEntity;
import com.sarmich.timetable.repository.SubjectRepository;
import com.sarmich.timetable.model.request.TeacherUpdateRequest;
import com.sarmich.timetable.utils.SpringSecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final ObjectMapper objectMapper;
    private final SubjectRepository subjectRepository;


    public TeacherResponse create(Integer orgId, TeacherRequest dto) {
        SubjectEntity subject = subjectRepository.findByIdAndOrgIdAndDeletedFalse(dto.subjectId(), orgId);
        if (subject == null) {
            throw new NotFoundException("Subject not found");
        }
        TeacherEntity entity = TeacherMapper.INSTANCE.toEntity(dto);
        entity.setOrgId(orgId);
        entity = teacherRepository.save(entity);
        return TeacherMapper.INSTANCE.toResponse(entity, objectMapper);
    }

    public TeacherResponse update(Integer orgId, TeacherUpdateRequest dto) {
        TeacherEntity old = teacherRepository.findByIdAndOrgIdAndDeletedFalse(dto.id(), orgId);
        if (old == null) {
            throw new NotFoundException("Teacher not found");
        }
        SubjectEntity subject = subjectRepository.findByIdAndOrgIdAndDeletedFalse(dto.subjectId(), orgId);
        if (subject == null) {
            throw new NotFoundException("Subject not found");
        }
        TeacherEntity entity = TeacherMapper.INSTANCE.toEntity(dto);
        entity.setOrgId(orgId);
        entity = teacherRepository.save(entity);
        return TeacherMapper.INSTANCE.toResponse(entity, objectMapper);
    }

    public void delete(Integer orgId, Integer id) {
        TeacherEntity teacher = teacherRepository.findByIdAndOrgIdAndDeletedFalse(id, orgId);
        if (teacher == null) {
            throw new NotFoundException("Teacher not found");
        }
        teacher.setDeleted(true);
        teacherRepository.save(teacher);
    }

    public TeacherResponse get(Integer orgId, Integer id) {
        TeacherEntity entity = teacherRepository.findByIdAndOrgIdAndDeletedFalse(id, orgId);
        if (entity == null) {
            throw new NotFoundException("Teacher not found");
        }
        return TeacherMapper.INSTANCE.toResponse(entity, objectMapper);

    }

    public List<TeacherResponse> findAll(Integer orgId) {
        return teacherRepository.findAllByOrgIdAndDeletedFalse(orgId).stream()
                .map(t -> TeacherMapper.INSTANCE.toResponse(t, objectMapper))
                .toList();
    }
}
