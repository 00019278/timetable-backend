package com.sarmich.timetable.teacher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarmich.timetable.exp.exception.NotFoundException;
import com.sarmich.timetable.mapper.TeacherMapper;
import com.sarmich.timetable.subject.SubjectEntity;
import com.sarmich.timetable.subject.SubjectRepository;
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


    public TeacherResponse create(TeacherRequest dto) {
        SubjectEntity subject = subjectRepository.findByIdAndProfileIdAndDeletedFalse(dto.subjectId(), SpringSecurityUtil.getProfileId());
        if (subject == null) {
            throw new NotFoundException("Subject not found");
        }
        TeacherEntity entity = TeacherMapper.INSTANCE.toEntity(dto);
        entity.setProfileId(SpringSecurityUtil.getProfileId());
        entity.setLessonTimes(objectMapper.convertValue(dto.lessonTimes(), new TypeReference<>() {
        }));
        entity = teacherRepository.save(entity);
        return TeacherMapper.INSTANCE.toResponse(entity, objectMapper);
    }

    public TeacherResponse update(TeacherUpdateRequest dto) {
        TeacherEntity old = teacherRepository.findByIdAndProfileIdAndDeletedFalse(dto.id(), SpringSecurityUtil.getProfileId());
        if (old == null) {
            throw new NotFoundException("Teacher not found");
        }
        SubjectEntity subject = subjectRepository.findByIdAndProfileIdAndDeletedFalse(dto.subjectId(), SpringSecurityUtil.getProfileId());
        if (subject == null) {
            throw new NotFoundException("Subject not found");
        }
        TeacherEntity entity = TeacherMapper.INSTANCE.toEntity(dto);
        entity.setLessonTimes(objectMapper.convertValue(dto.lessonTimes(), new TypeReference<>() {
        }));
        entity = teacherRepository.save(entity);
        return TeacherMapper.INSTANCE.toResponse(entity, objectMapper);
    }

    public void delete(Long id) {
        TeacherEntity teacher = teacherRepository.findByIdAndProfileIdAndDeletedFalse(id, SpringSecurityUtil.getProfileId());
        teacher.setDeleted(true);
        teacherRepository.save(teacher);
    }

    public TeacherResponse get(Long id) {
        return TeacherMapper.INSTANCE.toResponse(
                teacherRepository.findByIdAndProfileIdAndDeletedFalse(id, SpringSecurityUtil.getProfileId()), objectMapper);

    }

    public List<TeacherResponse> findAll() {
        return teacherRepository.findAllByProfileIdAndDeletedFalse(SpringSecurityUtil.getProfileId()).stream()
                .map(t -> TeacherMapper.INSTANCE.toResponse(t, objectMapper))
                .toList();
    }
}
