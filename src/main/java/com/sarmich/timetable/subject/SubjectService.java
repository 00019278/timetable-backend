package com.sarmich.timetable.subject;

import com.sarmich.timetable.exp.ItemNotFoundException;
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
        SubjectEntity entity = subjectRepository.save(SubjectMapper.INSTANCE.toEntity(request));
        return SubjectMapper.INSTANCE.toResponse(entity);
    }

    public SubjectResponse update(SubjectUpdateDto dto) {
        subjectRepository.findByIdAndProfileIdAndDeletedFalse(dto.getId(), SpringSecurityUtil.getProfileId());

        SubjectEntity entity = subjectRepository.save(SubjectMapper.INSTANCE.toEntity(dto));
        return SubjectMapper.INSTANCE.toResponse(entity);
    }

    public void delete(Long id) {
        subjectRepository.updateDeleted(id, SpringSecurityUtil.getProfileId());
    }

    public SubjectResponse get(Long id) {
        SubjectEntity entity = subjectRepository.findByIdAndProfileIdAndDeletedFalse(id, SpringSecurityUtil.getProfileId());

        return SubjectMapper.INSTANCE.toResponse(entity);
    }

    public PageImpl<SubjectResponse> getAll(Pageable page) {
        List<SubjectEntity> entities = subjectRepository.findAllByProfileIdAndDeletedFalse(SpringSecurityUtil.getProfileId(), page);
        List<SubjectResponse> responses = entities.stream().map(s -> SubjectMapper.INSTANCE.toResponse(s)).toList();
        long totalElements = subjectRepository.countByProfileIdAndDeletedFalse(SpringSecurityUtil.getProfileId());

        return new PageImpl<>(responses, page, totalElements);
    }
}
