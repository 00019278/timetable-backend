package com.sarmich.timetable.service;

import com.sarmich.timetable.domain.BuildingEntity;
import com.sarmich.timetable.domain.RoomEntity;
import com.sarmich.timetable.exception.NotFoundException;
import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.mapper.BuildingMapper;
import com.sarmich.timetable.mapper.RoomMapper;
import com.sarmich.timetable.model.request.RoomRequest;
import com.sarmich.timetable.model.response.BuildingResponse;
import com.sarmich.timetable.model.response.RoomResponse;
import com.sarmich.timetable.repository.BuildingRepository;
import com.sarmich.timetable.repository.ClassRoomRepository;
import com.sarmich.timetable.repository.RoomRepository;
import com.sarmich.timetable.utils.Util;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@AllArgsConstructor
public class RoomService {

  private final RoomRepository roomRepository;
  private final ClassRoomRepository classRoomRepository;
  private final BuildingRepository buildingRepository;

  @Transactional
  public RoomResponse add(Integer orgId, RoomRequest request) {
    log.debug("Adding room request {} for organization {}", request, orgId);
    RoomEntity entity = RoomMapper.INSTANCE.toEntity(request);
    entity.setOrgId(orgId);
    BuildingEntity building;
    if (request.buildingId() == null) {
      building =
          buildingRepository
              .findByOrgIdAndIsDefaultTrueAndDeletedFalse(orgId)
              .orElseThrow(
                  () ->
                      new NotFoundException(
                          ErrorCode.NOT_FOUND_ERROR_CODE, "Default building not found"));
      entity.setBuildingId(building.getId());
    } else {
      building =
          buildingRepository
              .findByIdAndOrgIdAndDeletedFalse(request.buildingId(), orgId)
              .orElseThrow(
                  () ->
                      new NotFoundException(ErrorCode.NOT_FOUND_ERROR_CODE, "Building not found"));
    }

    RoomEntity savedEntity = roomRepository.save(entity);
    log.info("Room with id {} added for organization {}", savedEntity.getId(), orgId);
    return RoomMapper.INSTANCE.toResponse(
        savedEntity, BuildingMapper.INSTANCE.toResponse(building));
  }

  @Transactional
  public void update(Integer orgId, Integer id, RoomRequest request) {
    log.debug("Updating room with id {} for organization {}. Request: {}", id, orgId, request);
    RoomEntity entity =
        roomRepository
            .findByIdAndOrgIdAndDeletedFalse(id, orgId)
            .orElseThrow(
                () -> new NotFoundException(ErrorCode.NOT_FOUND_ERROR_CODE, "Room not found"));
    entity.setName(request.name());
    entity.setShortName(request.shortName());
    entity.setType(request.type());
    entity.setAvailabilities(request.availabilities());

    roomRepository.save(entity);
  }

  @Transactional
  public void delete(Integer orgId, Integer id) {
    log.debug("Deleting room with id {} for organization {}", id, orgId);
    roomRepository.updateDeleted(id, orgId);
    classRoomRepository.deleteAllByRoomIdAndOrgId(id, orgId);
    log.info("Room with id {} deleted for organization {}", id, orgId);
  }

  public Page<RoomResponse> getAll(Integer orgId, Pageable pageable) {
    log.debug("Fetching all rooms for organization {} with pageable {}", orgId, pageable);
    Page<RoomEntity> rooms = roomRepository.findAllByOrgIdAndDeletedFalse(orgId, pageable);
    List<Integer> buildings = Util.idList(rooms.getContent(), RoomEntity::getBuildingId);
    List<BuildingResponse> builds =
        buildingRepository.findAllByIdIn(buildings).stream()
            .map(BuildingMapper.INSTANCE::toResponse)
            .toList();
    HashMap<Integer, BuildingResponse> map = Util.mapById(builds, BuildingResponse::id);
    return rooms.map(room -> RoomMapper.INSTANCE.toResponse(room, map.get(room.getBuildingId())));
  }

  public List<RoomResponse> getByIds(Integer orgId, List<Integer> ids) {
    log.debug("Fetching rooms with ids {} for organization {}", ids, orgId);
    return roomRepository.findAllByOrgIdAndIdInAndDeletedFalse(orgId, ids).stream()
        .map(RoomMapper.INSTANCE::toResponse)
        .toList();
  }

  public List<RoomResponse> findAll(Integer orgId) {
    log.debug("Fetching all rooms for organization  {}", orgId);
    return roomRepository.findAllByOrgIdAndDeletedFalse(orgId).stream()
        .map(RoomMapper.INSTANCE::toResponse)
        .toList();
  }
}
