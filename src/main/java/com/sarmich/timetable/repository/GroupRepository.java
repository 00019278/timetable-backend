package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.GroupEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Integer> {

  List<GroupEntity> findAllByClassIdAndOrgIdAndDeletedFalse(Integer classId, Integer orgId);

  List<GroupEntity> findAllByClassIdInAndOrgIdAndDeletedFalse(
      List<Integer> classIds, Integer orgId);

  @Modifying
  @Query("update GroupEntity g set g.deleted = true where g.id = :id and g.orgId = :orgId")
  void updateDeleted(Integer id, Integer orgId);

  @Modifying
  @Query(
      "update GroupEntity g set g.deleted = true where g.classId = :classId and g.orgId = :orgId")
  void deleteAllByClassIdAndOrgId(Integer classId, Integer orgId);
}
