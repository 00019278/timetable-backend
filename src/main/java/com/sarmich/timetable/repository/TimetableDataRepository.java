package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.TimetableDataEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TimetableDataRepository extends JpaRepository<TimetableDataEntity, Integer> {

  List<TimetableDataEntity> findAllByTimetableIdAndVersion(UUID timetableId, Integer version);

  @Query("select coalesce(max(version),0) from TimetableDataEntity  where timetableId = ?1")
  Integer findMaxVersion(UUID timetableId);

  void deleteAllByTimetableId(UUID timetableId);
}
