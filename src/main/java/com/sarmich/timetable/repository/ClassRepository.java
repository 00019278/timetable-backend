package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.ClassEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    ClassEntity findByIdAndOrgIdAndDeletedFalse(Integer id, Integer orgId);

    @Transactional
    @Modifying
    @Query("update ClassEntity set deleted = true where id = ?1 and orgId = ?2")
    void updateDeleted(Integer id, Integer profileId);

    List<ClassEntity> findAllByOrgIdAndDeletedFalse(Integer profileId, Pageable pageable);

    Long countByOrgIdAndDeletedFalse(Integer profileId);
}
