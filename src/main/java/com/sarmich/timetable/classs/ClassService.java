package com.sarmich.timetable.classs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarmich.timetable.exp.exception.NotFoundException;
import com.sarmich.timetable.mapper.ClassMapper;
import com.sarmich.timetable.utils.SpringSecurityUtil;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassService {

    private final ClassRepository classRepository;
    private final ObjectMapper objectMapper;

    public ClassService(ClassRepository classRepository, ObjectMapper objectMapper) {
        this.classRepository = classRepository;
        this.objectMapper = objectMapper;
    }

    public ClassResponse add(ClassRequest request) {
        ClassEntity entity = ClassMapper.INSTANCE.toEntity(request, objectMapper);
        entity.setProfileId(SpringSecurityUtil.getProfileId());
        return ClassMapper.INSTANCE.toResponse(classRepository.save(entity), objectMapper);
    }

    public ClassResponse update(ClassUpdateRequest request) {
        ClassEntity entity = classRepository.findByIdAndProfileIdAndDeletedFalse(request.id(), SpringSecurityUtil.getProfileId());
        if (entity == null) {
            throw new NotFoundException("Class not found");
        }
        return ClassMapper.INSTANCE.toResponse(ClassMapper.INSTANCE.toEntity(request, objectMapper), objectMapper);
    }

    public void delete(Long id) {
        classRepository.updateDeleted(id, SpringSecurityUtil.getProfileId());
    }

    public ClassResponse get(Long id) {
        ClassEntity entity = classRepository.findByIdAndProfileIdAndDeletedFalse(id, SpringSecurityUtil.getProfileId());
        if (entity == null) {
            throw new NotFoundException("Class not found");
        }
        return ClassMapper.INSTANCE.toResponse(entity, objectMapper);
    }

    public PageImpl<ClassResponse> getAll(Pageable pageable) {
        List<ClassEntity> classEntities = classRepository.findAllByProfileIdAndDeletedFalse(SpringSecurityUtil.getProfileId(), pageable);
        List<ClassResponse> classResponses = classEntities.stream()
                .map(m -> ClassMapper.INSTANCE.toResponse(m, objectMapper))
                .collect(Collectors.toList());
        return new PageImpl<>(classResponses, pageable, classRepository.countByProfileIdAndDeletedFalse(SpringSecurityUtil.getProfileId()));
    }
}
