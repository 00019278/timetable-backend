package com.sarmich.timetable.lesson;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<LessonEntity, Long> {


    @Transactional
    @Modifying
    @Query("update LessonEntity set deleted = true where id = ?1 and profileId = ?2")
    void updateDeleted(Long id, Integer profileId);

    List<LessonEntity> findAllByProfileIdAndDeletedFalse(Integer id, Pageable page);

    long countByProfileIdAndDeletedFalse(Integer id);

    LessonEntity findByIdAndProfileIdAndDeletedFalse(Long id, Integer profileId);
}
