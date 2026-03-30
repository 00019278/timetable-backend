package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.BuildingEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BuildingRepository extends JpaRepository<BuildingEntity, Integer> {

  Optional<BuildingEntity> findByIdAndOrgIdAndDeletedFalse(Integer id, Integer orgId);

  Page<BuildingEntity> findAllByOrgIdAndDeletedFalse(Integer orgId, Pageable pageable);

  List<BuildingEntity> findAllByOrgIdAndDeletedFalse(Integer orgId);

  List<BuildingEntity> findAllByIdIn(List<Integer> ids);

  Optional<BuildingEntity> findByOrgIdAndIsDefaultTrueAndDeletedFalse(Integer orgId);

  @Modifying
  @Query("update BuildingEntity set deleted = true where id = ?1 and orgId = ?2")
  void updateDeleted(Integer id, Integer orgId);
}
