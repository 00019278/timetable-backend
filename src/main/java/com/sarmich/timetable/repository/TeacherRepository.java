package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.TeacherEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<TeacherEntity, Integer> {
  TeacherEntity findByIdAndOrgIdAndDeletedFalse(Integer id, Integer profileId);

  List<TeacherEntity> findAllByOrgIdAndDeletedFalse(Integer profileId);

  Page<TeacherEntity> findAllByOrgIdAndDeletedFalse(Integer profileId, Pageable pageable);

  List<TeacherEntity> findAllByOrgIdAndIdInAndDeletedFalse(Integer orgId, List<Integer> ids);

  Optional<TeacherEntity> findFirstByOrgIdAndDeletedFalseAndFullNameIgnoreCase(
      Integer orgId, String fullName);
}
