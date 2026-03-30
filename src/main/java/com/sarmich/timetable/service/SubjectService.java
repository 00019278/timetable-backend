package com.sarmich.timetable.service;

import com.sarmich.timetable.domain.SubjectEntity;
import com.sarmich.timetable.exception.NotFoundException;
import com.sarmich.timetable.mapper.SubjectMapper;
import com.sarmich.timetable.model.request.SubjectRequest;
import com.sarmich.timetable.model.response.SubjectResponse;
import com.sarmich.timetable.repository.SubjectRepository;
import com.sarmich.timetable.model.request.SubjectUpdateRequest;
import com.sarmich.timetable.utils.SpringSecurityUtil;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public SubjectResponse add(Integer orgId, SubjectRequest request) {
        SubjectEntity entity = SubjectMapper.INSTANCE.toEntity(request);
        entity.setOrgId(orgId);
        entity = subjectRepository.save(entity);
        return SubjectMapper.INSTANCE.toResponse(entity);
    }

    public SubjectResponse update(Integer orgId, Integer id, SubjectRequest dto) {
        SubjectEntity old = subjectRepository.findByIdAndOrgIdAndDeletedFalse(id, orgId);
        if (old == null) {
            throw new NotFoundException("Subject not found");
        }
        SubjectEntity entity = SubjectMapper.INSTANCE.toEntity(dto);
        entity.setOrgId(orgId);
        entity = subjectRepository.save(entity);
        return SubjectMapper.INSTANCE.toResponse(entity);
    }

    public void delete(Integer orgId, Integer id) {
        subjectRepository.updateDeleted(id, orgId);
    }

    public SubjectResponse get(Integer orgId, Integer id) {
        SubjectEntity entity = subjectRepository.findByIdAndOrgIdAndDeletedFalse(id, orgId);
        if (entity == null) {
            throw new NotFoundException("Not found");
        }
        return SubjectMapper.INSTANCE.toResponse(entity);
    }

    public PageImpl<SubjectResponse> getAll(Integer orgId, Pageable page) {
        List<SubjectEntity> entities = subjectRepository.findAllByOrgIdAndDeletedFalse(orgId, page);
        List<SubjectResponse> responses = entities.stream().map(SubjectMapper.INSTANCE::toResponse).toList();
        long totalElements = subjectRepository.countByOrgIdAndDeletedFalse(orgId);

        return new PageImpl<>(responses, page, totalElements);
    }
}
