package com.sarmich.timetable.teacher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarmich.timetable.exp.ItemNotFoundException;
import com.sarmich.timetable.utils.SpringSecurityUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final ObjectMapper objectMapper;

    public TeacherService(TeacherRepository teacherRepository, ObjectMapper objectMapper) {
        this.teacherRepository = teacherRepository;
        this.objectMapper = objectMapper;
    }

    public TeacherResponse create(TeacherRequest dto) {
        TeacherEntity entity = TeacherMapper.INSTANCE.toEntity(dto);
        entity.setProfileId(SpringSecurityUtil.getProfileId());
        entity.setTimeSlots(objectMapper.convertValue(dto.getTimeSlotList(), new TypeReference<Map<String, Object>>() {
        }));
        entity = teacherRepository.save(entity);
        return TeacherMapper.INSTANCE.toResponse(entity);
    }

    public TeacherResponse update(TeacherUpdateRequest dto) {
        TeacherEntity old = teacherRepository.findByIdAndDeletedFalse(dto.getId());
        TeacherEntity entity = TeacherMapper.INSTANCE.toEntity(dto);
        entity.setTimeSlots(objectMapper.convertValue(dto.getTimeSlotList(), new TypeReference<Map<String, Object>>() {
        }));
        entity = teacherRepository.save(entity);
        return TeacherMapper.INSTANCE.toResponse(entity);
    }

    public void delete(Long id) {
        TeacherEntity teacher = teacherRepository.findByIdAndDeletedFalse(id);
        teacher.setDeleted(true);
        teacherRepository.save(teacher);
    }

    public TeacherResponse get(Long id) {
        return TeacherMapper.INSTANCE.toResponse(
                teacherRepository.findByIdAndDeletedFalse(id));

    }

    public List<TeacherResponse> findAll() {
        return teacherRepository.findAllByProfileIdAndDeletedFalse(SpringSecurityUtil.getProfileId()).stream()
                .map(TeacherMapper.INSTANCE::toResponse)
                .toList();
    }
}
