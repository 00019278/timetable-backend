package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.ClassEntity;
import com.sarmich.timetable.model.TimeSlot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ClassRepository extends JpaRepository<ClassEntity, Integer> {

  ClassEntity findByIdAndOrgIdAndDeletedFalse(Integer id, Integer orgId);

  List<ClassEntity> findByIdInAndOrgIdAndDeletedFalse(List<Integer> id, Integer orgId);

  @Modifying
  @Query("update ClassEntity set deleted = true where id = ?1 and orgId = ?2")
  void updateDeleted(Integer id, Integer profileId);

  List<ClassEntity> findAllByOrgIdAndDeletedFalse(Integer profileId, Pageable pageable);

  List<ClassEntity> findAllByOrgIdAndIdInAndDeletedFalse(Integer profileId, List<Integer> ids);

  List<ClassEntity> findAllByOrgIdAndDeletedFalse(Integer orgId);

  Long countByOrgIdAndDeletedFalse(Integer profileId);

  Optional<ClassEntity> findFirstByOrgIdAndDeletedFalseAndNameIgnoreCase(
      Integer orgId, String name);

  @Modifying
  @Query("update ClassEntity set availabilities = ?3 where id in (?2) and orgId = ?1")
  void applyToOthers(Integer orgId, List<Integer> longs, List<TimeSlot> timeSlots);
}
