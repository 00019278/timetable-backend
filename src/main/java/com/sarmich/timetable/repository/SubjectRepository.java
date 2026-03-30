package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.SubjectEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface SubjectRepository extends JpaRepository<SubjectEntity, Integer> {

  @Transactional
  @Modifying
  @Query("update SubjectEntity set name = ?2, shortName = ?3 where id = ?1 and orgId = ?4")
  int update(Long id, String name, String shortName, Integer profileId);

  @Transactional
  @Modifying
  @Query("update SubjectEntity set deleted = true where id = ?1 and orgId = ?2")
  void updateDeleted(Integer id, Integer profileId);

  @Query("select s from SubjectEntity s where s.id = ?1 and s.orgId = ?2 and s.deleted = false")
  Optional<SubjectEntity> get(Long id, Integer profileId);

  List<SubjectEntity> findAllByOrgIdAndDeletedFalse(Integer id, Pageable page);

  List<SubjectEntity> findAllByOrgIdAndDeletedFalse(Integer id);

  long countByOrgIdAndDeletedFalse(Integer id);

  SubjectEntity findByIdAndOrgIdAndDeletedFalse(Integer id, Integer orgId);

  List<SubjectEntity> findAllByOrgIdAndIdInAndDeletedFalse(Integer orgId, List<Integer> ids);

  Optional<SubjectEntity> findFirstByOrgIdAndDeletedFalseAndNameIgnoreCase(
      Integer orgId, String name);
}
