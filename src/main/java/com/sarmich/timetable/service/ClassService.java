package com.sarmich.timetable.service;

import com.sarmich.timetable.domain.ClassEntity;
import com.sarmich.timetable.exception.NotFoundException;
import com.sarmich.timetable.mapper.ClassMapper;
import com.sarmich.timetable.model.request.ClassRequest;
import com.sarmich.timetable.model.response.ClassResponse;
import com.sarmich.timetable.repository.ClassRepository;
import com.sarmich.timetable.utils.SpringSecurityUtil;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@AllArgsConstructor
public class ClassService {
    private final ClassRepository classRepository;


    public ClassResponse add(Integer orgId, ClassRequest request) {
        ClassEntity entity = ClassMapper.INSTANCE.toEntity(request);
        entity.setOrgId(orgId);
        return ClassMapper.INSTANCE.toResponse(classRepository.save(entity));
    }

    public ClassResponse update(Integer orgId, Integer id, ClassRequest request) {
        ClassEntity entity = classRepository.findByIdAndOrgIdAndDeletedFalse(id, orgId);
        if (entity == null) {
            throw new NotFoundException("Class not found");
        }
        // update mutable fields
        entity.setName(request.name());
        entity.setShortName(request.shortName());
        entity.setAvailabilities(request.availabilities());
        entity.setOrgId(orgId);
        ClassEntity saved = classRepository.save(entity);
        return ClassMapper.INSTANCE.toResponse(saved);
    }

    public void delete(Integer orgId, Integer id) {
        log.debug("Delete class with id: {}", id);
        classRepository.updateDeleted(id, orgId);
    }

    public ClassResponse get(Integer orgId, Integer id) {
        ClassEntity entity = classRepository.findByIdAndOrgIdAndDeletedFalse(id, orgId);
        if (entity == null) {
            throw new NotFoundException("Class not found");
        }
        return ClassMapper.INSTANCE.toResponse(entity);
    }

    public PageImpl<ClassResponse> getAll(Integer orgId, Pageable pageable) {
        List<ClassEntity> classEntities = classRepository.findAllByOrgIdAndDeletedFalse(orgId, pageable);
        List<ClassResponse> classResponses = classEntities.stream()
            .map(ClassMapper.INSTANCE::toResponse)
            .toList();
        return new PageImpl<>(classResponses, pageable, classRepository.countByOrgIdAndDeletedFalse(orgId));
    }
}
