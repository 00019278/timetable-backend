package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.ClassEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    ClassEntity findByIdAndProfileIdAndDeletedFalse(Long id, Integer profileId);

    @Transactional
    @Modifying
    @Query("update ClassEntity set deleted = true where id = ?1 and profileId = ?2")
    void updateDeleted(Long id, Integer profileId);

    List<ClassEntity> findAllByProfileIdAndDeletedFalse(Integer profileId, Pageable pageable);

    Long countByProfileIdAndDeletedFalse(Integer profileId);
}
