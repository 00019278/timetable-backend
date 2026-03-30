package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.RoomEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface RoomRepository extends CrudRepository<RoomEntity, Integer> {
  Optional<RoomEntity> findFirstByOrgIdAndNameIgnoreCase(Integer orgId, String name);

  List<RoomEntity> findAllByIdInAndDeletedFalse(List<Integer> orgId);

  Optional<RoomEntity> findByIdAndOrgIdAndDeletedFalse(Integer id, Integer orgId);

  @Modifying
  @Query("update RoomEntity set deleted = true where id = ?1 and orgId = ?2")
  void updateDeleted(Integer id, Integer orgId);

  Page<RoomEntity> findAllByOrgIdAndDeletedFalse(Integer orgId, Pageable pageable);

  List<RoomEntity> findAllByOrgIdAndDeletedFalse(Integer orgId);

  List<RoomEntity> findAllByOrgIdAndIdInAndDeletedFalse(Integer orgId, List<Integer> ids);
}
