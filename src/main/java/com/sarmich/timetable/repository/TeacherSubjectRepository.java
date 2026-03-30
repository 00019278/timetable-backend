package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.TeacherSubjectEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherSubjectRepository extends JpaRepository<TeacherSubjectEntity, Integer> {

  List<TeacherSubjectEntity> findAllByTeacherIdAndDeletedFalse(Integer teacherId);

  void deleteByTeacherIdAndSubjectIdIn(Integer teacherId, List<Integer> subjectIds);

  List<TeacherSubjectEntity> findAllByTeacherIdInAndDeletedFalse(List<Integer> teacherIds);
}
