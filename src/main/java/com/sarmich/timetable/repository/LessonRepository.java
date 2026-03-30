package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.LessonEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LessonRepository extends JpaRepository<LessonEntity, Long> {


    @Transactional
    @Modifying
    @Query("update LessonEntity set deleted = true where id = ?1 and orgId = ?2")
    void updateDeleted(Integer id, Integer profileId);

    List<LessonEntity> findAllByOrgIdAndDeletedFalse(Integer id, Pageable page);

    long countByOrgIdAndDeletedFalse(Integer id);

    LessonEntity findByIdAndOrgIdAndDeletedFalse(Integer id, Integer orgId);
}
