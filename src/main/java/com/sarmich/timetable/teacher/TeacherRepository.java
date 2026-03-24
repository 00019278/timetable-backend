package com.sarmich.timetable.teacher;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherRepository extends JpaRepository<TeacherEntity, Long> {
    TeacherEntity findByIdAndDeletedFalse(Long id);
    List<TeacherEntity> findAllByProfileIdAndDeletedFalse(Integer profileId);
}
