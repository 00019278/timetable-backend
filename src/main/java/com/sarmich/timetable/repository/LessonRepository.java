package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.LessonEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonRepository extends JpaRepository<LessonEntity, Integer> {
  void deleteByIdAndOrgId(Integer id, Integer orgId);

  @Query(
      "select l from LessonEntity l where l.orgId = :orgId and l.classId = :classId and l.teacherId = :teacherId and l.subjectId = :subjectId and l.deleted = false")
  LessonEntity findByUniqueKey(
      @Param("orgId") Integer orgId,
      @Param("classId") Integer classId,
      @Param("teacherId") Integer teacherId,
      @Param("subjectId") Integer subjectId);

  List<LessonEntity> findAllByOrgIdAndDeletedFalse(Integer id, Pageable page);

  List<LessonEntity> findAllByOrgIdAndDeletedFalse(Integer id);

  long countByOrgIdAndDeletedFalse(Integer id);

  LessonEntity findByIdAndOrgIdAndDeletedFalse(Integer id, Integer orgId);
}
