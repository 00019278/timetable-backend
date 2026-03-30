package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.TimetableEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimetableRepository extends JpaRepository<TimetableEntity, UUID> {
  TimetableEntity findByIdAndDeletedFalse(UUID id);

  List<TimetableEntity> findAllByOrgIdAndDeletedFalse(Integer orgId);
}
