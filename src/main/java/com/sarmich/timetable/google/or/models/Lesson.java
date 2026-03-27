package com.sarmich.timetable.google.or.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Lesson {
  Integer id;
  Integer count;
  Integer teacherId;
  Integer subjectId;
  Integer classId;

  public Lesson(
      final Integer id,
      final Integer count,
      final Integer teacherId,
      final Integer subjectId,
      final Integer classId) {
    this.id = id;
    this.count = count;
    this.teacherId = teacherId;
    this.subjectId = subjectId;
    this.classId = classId;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Lesson lesson = (Lesson) o;
    return Objects.equals(teacherId, lesson.teacherId)
        && Objects.equals(subjectId, lesson.subjectId)
        && Objects.equals(classId, lesson.classId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, count, teacherId, subjectId, classId);
  }
}
