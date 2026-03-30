package com.sarmich.timetable.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarmich.timetable.domain.ClassEntity;
import com.sarmich.timetable.domain.LessonEntity;
import com.sarmich.timetable.domain.SubjectEntity;
import com.sarmich.timetable.domain.TeacherEntity;
import com.sarmich.timetable.domain.RoomEntity;
import com.sarmich.timetable.exception.NotFoundException;
import com.sarmich.timetable.mapper.ClassMapper;
import com.sarmich.timetable.mapper.LessonMapper;
import com.sarmich.timetable.mapper.SubjectMapper;
import com.sarmich.timetable.mapper.TeacherMapper;
import com.sarmich.timetable.model.request.LessonRequest;
import com.sarmich.timetable.model.request.LessonUpdateRequest;
import com.sarmich.timetable.model.response.ClassResponse;
import com.sarmich.timetable.model.response.LessonResponse;
import com.sarmich.timetable.model.response.RoomResponse;
import com.sarmich.timetable.model.response.SubjectResponse;
import com.sarmich.timetable.model.response.TeacherResponse;
import com.sarmich.timetable.repository.ClassRepository;
import com.sarmich.timetable.repository.LessonRepository;
import com.sarmich.timetable.repository.RoomRepository;
import com.sarmich.timetable.repository.SubjectRepository;
import com.sarmich.timetable.repository.TeacherRepository;
import com.sarmich.timetable.utils.SpringSecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final ClassRepository classRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final RoomRepository roomRepository;
    private final ObjectMapper objectMapper;

    public LessonResponse add(Integer orgId, LessonRequest request) {
        ClassEntity classEntity = classRepository.findByIdAndOrgIdAndDeletedFalse(request.classId(), orgId);
        if (classEntity == null) {
            throw new NotFoundException("Class not found");
        }
        TeacherEntity teacherEntity = teacherRepository.findByIdAndOrgIdAndDeletedFalse(request.teacherId(), orgId);
        if (teacherEntity == null) {
            throw new NotFoundException("Teacher not found");
        }
        SubjectEntity subject = subjectRepository.findByIdAndOrgIdAndDeletedFalse(request.subjectId(), orgId);
        if (subject == null) {
            throw new NotFoundException("Subject not found");
        }

        LessonEntity entity = LessonMapper.INSTANCE.toEntity(request);
        // Always set orgId from auth principal for safety
        entity.setOrgId(orgId);
        entity = lessonRepository.save(entity);
        return mapToResponse(entity);
    }

    public LessonResponse update(Integer orgId, LessonUpdateRequest request) {
        LessonEntity lesson = lessonRepository.findByIdAndOrgIdAndDeletedFalse(request.id(), orgId);
        if (lesson == null) {
            throw new NotFoundException("Lesson not found");
        }
        ClassEntity classEntity = classRepository.findByIdAndOrgIdAndDeletedFalse(request.classId(), orgId);
        if (classEntity == null) {
            throw new NotFoundException("Class not found");
        }
        TeacherEntity teacherEntity = teacherRepository.findByIdAndOrgIdAndDeletedFalse(request.teacherId(), orgId);
        if (teacherEntity == null) {
            throw new NotFoundException("Teacher not found");
        }
        SubjectEntity subject = subjectRepository.findByIdAndOrgIdAndDeletedFalse(request.subjectId(), orgId);
        if (subject == null) {
            throw new NotFoundException("Subject not found");
        }
        LessonEntity entity = LessonMapper.INSTANCE.toEntity(request);
        entity.setId(lesson.getId());
        entity.setOrgId(orgId);
        entity = lessonRepository.save(entity);
        return mapToResponse(entity);
    }

    public void delete(Integer orgId, Integer id) {
        lessonRepository.updateDeleted(id, orgId);
    }

    public LessonResponse get(Integer orgId, Integer id) {
        LessonEntity entity = lessonRepository.findByIdAndOrgIdAndDeletedFalse(id, orgId);
        if (entity == null) {
            throw new NotFoundException("Not found");
        }
        return mapToResponse(entity);
    }

    public PageImpl<LessonResponse> getAll(Integer orgId, Pageable page) {
        List<LessonEntity> entities = lessonRepository.findAllByOrgIdAndDeletedFalse(orgId, page);
        List<LessonResponse> responses = entities.stream().map(this::mapToResponse).toList();
        long totalElements = lessonRepository.countByOrgIdAndDeletedFalse(orgId);

        return new PageImpl<>(responses, page, totalElements);
    }

    private LessonResponse mapToResponse(LessonEntity entity) {
        ClassEntity classEntity = classRepository.findById(Long.valueOf(entity.getClassId())).orElse(null);
        TeacherEntity teacherEntity = teacherRepository.findById(Long.valueOf(entity.getTeacherId())).orElse(null);
        SubjectEntity subjectEntity = subjectRepository.findById(Long.valueOf(entity.getSubjectId())).orElse(null);

        List<RoomResponse> rooms = new ArrayList<>();
        if (entity.getRoomIds() != null && !entity.getRoomIds().isEmpty()) {
            Iterable<RoomEntity> roomEntities = roomRepository.findAllById(entity.getRoomIds());
            for (RoomEntity r : roomEntities) {
                rooms.add(new RoomResponse(r.getId(), r.getName(), r.getShortName()));
            }
        }

        ClassResponse classInfo = classEntity != null ? ClassMapper.INSTANCE.toResponse(classEntity) : null;
        TeacherResponse teacherInfo = teacherEntity != null ? TeacherMapper.INSTANCE.toResponse(teacherEntity, objectMapper) : null;
        SubjectResponse subjectInfo = subjectEntity != null ? SubjectMapper.INSTANCE.toResponse(subjectEntity) : null;

        return new LessonResponse(
                entity.getId(),
                entity.getOrgId(),
                classInfo,
                teacherInfo,
                rooms,
                subjectInfo,
                entity.getLessonCount(),
                entity.getDayOfWeek(),
                entity.getHour(),
                entity.getPeriod(),
                entity.getCreatedDate(),
                entity.getUpdatedDate()
        );
    }
}
