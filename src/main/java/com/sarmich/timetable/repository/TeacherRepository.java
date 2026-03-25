package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherRepository extends JpaRepository<TeacherEntity, Long> {
    TeacherEntity findByIdAndProfileIdAndDeletedFalse(Long id,Integer profileId);
    List<TeacherEntity> findAllByProfileIdAndDeletedFalse(Integer profileId);
}
