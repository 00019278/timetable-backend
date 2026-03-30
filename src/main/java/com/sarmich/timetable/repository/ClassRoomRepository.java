package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.ClassRoomEntity;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassRoomRepository extends JpaRepository<ClassRoomEntity, Integer> {

  void deleteAllByClassIdAndOrgId(Integer classId, Integer orgId);

  void deleteAllByClassIdAndRoomIdIn(Integer classId, Set<Integer> orgId);

  List<ClassRoomEntity> findAllByClassIdIn(List<Integer> classIds);

  void deleteAllByRoomIdAndOrgId(Integer id, Integer orgId);
}
