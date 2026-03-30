package com.sarmich.timetable.service;

import com.sarmich.timetable.domain.BuildingEntity;
import com.sarmich.timetable.exception.NotFoundException;
import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.mapper.BuildingMapper;
import com.sarmich.timetable.model.request.BuildingRequest;
import com.sarmich.timetable.model.request.BuildingUpdateRequest;
import com.sarmich.timetable.model.response.BuildingResponse;
import com.sarmich.timetable.repository.BuildingRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class BuildingService {

  private final BuildingRepository buildingRepository;

  @Transactional
  public BuildingResponse add(Integer orgId, BuildingRequest request) {
    log.debug("Adding building request {} for organization {}", request, orgId);
    BuildingEntity entity = BuildingMapper.INSTANCE.toEntity(request);
    entity.setOrgId(orgId);
    return BuildingMapper.INSTANCE.toResponse(buildingRepository.save(entity));
  }

  @Transactional
  public BuildingResponse update(Integer orgId, Integer id, BuildingUpdateRequest request) {
    log.debug("Updating building with id {} for organization {}. Request: {}", id, orgId, request);
    BuildingEntity entity =
        buildingRepository
            .findByIdAndOrgIdAndDeletedFalse(id, orgId)
            .orElseThrow(
                () -> new NotFoundException(ErrorCode.NOT_FOUND_ERROR_CODE, "Building not found"));

    entity.setName(request.name());
    return BuildingMapper.INSTANCE.toResponse(buildingRepository.save(entity));
  }

  @Transactional
  public void delete(Integer orgId, Integer id) {
    log.debug("Deleting building with id {} for organization {}", id, orgId);
    buildingRepository.updateDeleted(id, orgId);
  }

  public Page<BuildingResponse> getAll(Integer orgId, Pageable pageable) {
    log.debug("Fetching all buildings for organization {} with pageable {}", orgId, pageable);
    return buildingRepository
        .findAllByOrgIdAndDeletedFalse(orgId, pageable)
        .map(BuildingMapper.INSTANCE::toResponse);
  }

  public List<BuildingResponse> findAll(Integer orgId) {
    log.debug("Fetching all buildings for organization {}", orgId);
    return buildingRepository.findAllByOrgIdAndDeletedFalse(orgId).stream()
        .map(BuildingMapper.INSTANCE::toResponse)
        .toList();
  }

  public BuildingResponse getById(Integer orgId, Integer id) {
    log.debug("Fetching building with id {} for organization {}", id, orgId);
    return buildingRepository
        .findByIdAndOrgIdAndDeletedFalse(id, orgId)
        .map(BuildingMapper.INSTANCE::toResponse)
        .orElseThrow(
            () -> new NotFoundException(ErrorCode.NOT_FOUND_ERROR_CODE, "Building not found"));
  }

  public void createDefaultBuilding(Integer orgId) {
    BuildingEntity defaultBuilding =
        BuildingEntity.builder()
            .name("Asosiy Bino")
            .orgId(orgId)
            .isDefault(true)
            .deleted(Boolean.FALSE)
            .build();
    buildingRepository.save(defaultBuilding);
  }
}
