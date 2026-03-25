package com.sarmich.timetable.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarmich.timetable.domain.ClassEntity;
import com.sarmich.timetable.domain.LessonEntity;
import com.sarmich.timetable.model.request.LessonRequest;
import com.sarmich.timetable.model.request.LessonUpdateRequest;
import com.sarmich.timetable.model.response.LessonInfo;
import com.sarmich.timetable.model.response.LessonResponse;
import com.sarmich.timetable.repository.ClassRepository;
import com.sarmich.timetable.exp.exception.NotFoundException;
import com.sarmich.timetable.mapper.ClassMapper;
import com.sarmich.timetable.mapper.LessonMapper;
import com.sarmich.timetable.mapper.SubjectMapper;
import com.sarmich.timetable.mapper.TeacherMapper;
import com.sarmich.timetable.repository.LessonRepository;
import com.sarmich.timetable.domain.SubjectEntity;
import com.sarmich.timetable.repository.SubjectRepository;
import com.sarmich.timetable.domain.TeacherEntity;
import com.sarmich.timetable.repository.TeacherRepository;
import com.sarmich.timetable.utils.SpringSecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ClassRepository classRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final ObjectMapper objectMapper;


    public LessonResponse add(LessonRequest request) {
        Integer profileId = SpringSecurityUtil.getProfileId();
        ClassEntity classEntity = classRepository.findByIdAndProfileIdAndDeletedFalse(request.classId(), profileId);
        if (classEntity == null) {
            throw new NotFoundException("Class not found");
        }
        TeacherEntity teacherEntity = teacherRepository.findByIdAndProfileIdAndDeletedFalse(request.teacherId(), profileId);
        if (teacherEntity == null) {
            throw new NotFoundException("Teacher not found");
        }
        SubjectEntity subject = subjectRepository.findByIdAndProfileIdAndDeletedFalse(request.subjectId(), profileId);
        if (subject == null) {
            throw new NotFoundException("Subject not found");
        }

            LessonEntity entity = LessonMapper.INSTANCE.toEntity(request);
        entity.setProfileId(SpringSecurityUtil.getProfileId());
        LessonInfo lessonInfo = new LessonInfo(TeacherMapper.INSTANCE.toResponse(teacherEntity), SubjectMapper.INSTANCE.toResponse(subject), ClassMapper.INSTANCE.toResponse(classEntity));
        entity.setInfoJson(objectMapper.convertValue(lessonInfo, new TypeReference<>() {
        }));
        entity = lessonRepository.save(entity);
        return LessonMapper.INSTANCE.toResponse(entity, lessonInfo);
    }

    public LessonResponse update(LessonUpdateRequest request) {
        Integer profileId = SpringSecurityUtil.getProfileId();
        LessonEntity lesson = lessonRepository.findByIdAndProfileIdAndDeletedFalse(request.id(), SpringSecurityUtil.getProfileId());
        if (lesson == null) {
            throw new NotFoundException("Lesson not found");
        }
        ClassEntity classEntity = classRepository.findByIdAndProfileIdAndDeletedFalse(request.classId(), profileId);
        if (classEntity == null) {
            throw new NotFoundException("Class not found");
        }
        TeacherEntity teacherEntity = teacherRepository.findByIdAndProfileIdAndDeletedFalse(request.teacherId(), profileId);
        if (teacherEntity == null) {
            throw new NotFoundException("Class not found");
        }
        SubjectEntity subject = subjectRepository.findByIdAndProfileIdAndDeletedFalse(request.subjectId(), profileId);
        if (subject == null) {
            throw new NotFoundException("Class not found");
        }
        LessonEntity entity = LessonMapper.INSTANCE.toEntity(request);
        entity.setProfileId(SpringSecurityUtil.getProfileId());
        LessonInfo lessonInfo = new LessonInfo(TeacherMapper.INSTANCE.toResponse(teacherEntity), SubjectMapper.INSTANCE.toResponse(subject), ClassMapper.INSTANCE.toResponse(classEntity));
        entity.setInfoJson(objectMapper.convertValue(lessonInfo, new TypeReference<>() {
        }));
        entity.setId(lesson.getId());
        entity = lessonRepository.save(entity);
        return LessonMapper.INSTANCE.toResponse(entity, lessonInfo);
    }

    public void delete(Long id) {
        lessonRepository.updateDeleted(id, SpringSecurityUtil.getProfileId());
    }

    public LessonResponse get(Long id) {
        LessonEntity entity = lessonRepository.findByIdAndProfileIdAndDeletedFalse(id, SpringSecurityUtil.getProfileId());
        if (entity == null) {
            throw new NotFoundException("Not found");
        }
        return LessonMapper.INSTANCE.toResponse(entity, objectMapper.convertValue(entity.getInfoJson(), LessonInfo.class));
    }

    public PageImpl<LessonResponse> getAll(Pageable page) {
        List<LessonEntity> entities = lessonRepository.findAllByProfileIdAndDeletedFalse(SpringSecurityUtil.getProfileId(), page);
        List<LessonResponse> responses = entities.stream().map(l -> LessonMapper.INSTANCE.toResponse(l, objectMapper.convertValue(l.getInfoJson(), LessonInfo.class))).toList();
        long totalElements = lessonRepository.countByProfileIdAndDeletedFalse(SpringSecurityUtil.getProfileId());

        return new PageImpl<>(responses, page, totalElements);
    }
}
