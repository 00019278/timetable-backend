package com.sarmich.timetable.subject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<SubjectEntity, Long> {

    @Transactional
    @Modifying
    @Query("update SubjectEntity set name = ?2, shortName = ?3 where id = ?1 and profileId = ?4")
    int update(Long id, String name, String shortName, Integer profileId);

    @Transactional
    @Modifying
    @Query("update SubjectEntity set deleted = true where id = ?1 and profileId = ?2")
    void updateDeleted(Long id, Integer profileId);

    @Query("select s from SubjectEntity s where s.id = ?1 and s.profileId = ?2 and s.deleted = false")
    Optional<SubjectEntity> get(Long id, Integer profileId);

    @Query("select s from SubjectEntity s where s.profileId = ?1 and s.deleted = false")
    Page<SubjectEntity> getAll(Integer profileId, Pageable page);

    List<SubjectEntity> findAllByProfileIdAndDeletedFalse(Integer id, Pageable page);

    long countByProfileIdAndDeletedFalse(Integer id);

    SubjectEntity findByIdAndProfileIdAndDeletedFalse(Long id, Integer profileId);
}
