package com.sarmich.timetable.service;

import com.sarmich.timetable.domain.SubjectEntity;
import com.sarmich.timetable.exp.exception.NotFoundException;
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

    public SubjectResponse add(SubjectRequest request) {
        SubjectEntity entity = SubjectMapper.INSTANCE.toEntity(request);
        entity.setProfileId(SpringSecurityUtil.getProfileId());
        entity = subjectRepository.save(entity);
        return SubjectMapper.INSTANCE.toResponse(entity);
    }

    public SubjectResponse update(SubjectUpdateRequest dto) {
        subjectRepository.findByIdAndProfileIdAndDeletedFalse(dto.id(), SpringSecurityUtil.getProfileId());

        SubjectEntity entity = subjectRepository.save(SubjectMapper.INSTANCE.toEntity(dto));
        return SubjectMapper.INSTANCE.toResponse(entity);
    }

    public void delete(Long id) {
        subjectRepository.updateDeleted(id, SpringSecurityUtil.getProfileId());
    }

    public SubjectResponse get(Long id) {
        SubjectEntity entity = subjectRepository.findByIdAndProfileIdAndDeletedFalse(id, SpringSecurityUtil.getProfileId());
        if (entity == null) {
            throw new NotFoundException("Not found");
        }
        return SubjectMapper.INSTANCE.toResponse(entity);
    }

    public PageImpl<SubjectResponse> getAll(Pageable page) {
        List<SubjectEntity> entities = subjectRepository.findAllByProfileIdAndDeletedFalse(SpringSecurityUtil.getProfileId(), page);
        List<SubjectResponse> responses = entities.stream().map(SubjectMapper.INSTANCE::toResponse).toList();
        long totalElements = subjectRepository.countByProfileIdAndDeletedFalse(SpringSecurityUtil.getProfileId());

        return new PageImpl<>(responses, page, totalElements);
    }
}
