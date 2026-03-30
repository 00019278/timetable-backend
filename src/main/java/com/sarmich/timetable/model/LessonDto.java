package com.sarmich.timetable.model;

import java.util.Objects;

public class LessonDto {
  private String teacherName;
  private String subjectName;
  private String className;
  private String room;
  private double hoursPerWeek;

  public LessonDto() {}

  public LessonDto(
      String teacherName, String subjectName, String className, String room, double hoursPerWeek) {
    this.teacherName = teacherName;
    this.subjectName = subjectName;
    this.className = className;
    this.room = room;
    this.hoursPerWeek = hoursPerWeek;
  }

  public String getTeacherName() {
    return teacherName;
  }

  public void setTeacherName(String teacherName) {
    this.teacherName = teacherName;
  }

  public String getSubjectName() {
    return subjectName;
  }

  public void setSubjectName(String subjectName) {
    this.subjectName = subjectName;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getRoom() {
    return room;
  }

  public void setRoom(String room) {
    this.room = room;
  }

  public double getHoursPerWeek() {
    return hoursPerWeek;
  }

  public void setHoursPerWeek(double hoursPerWeek) {
    this.hoursPerWeek = hoursPerWeek;
  }

  @Override
  public String toString() {
    return "{"
        + "\"teacherName\":\""
        + (teacherName == null ? "" : teacherName)
        + "\","
        + "\"subjectName\":\""
        + (subjectName == null ? "" : subjectName)
        + "\","
        + "\"className\":\""
        + (className == null ? "" : className)
        + "\","
        + "\"room\":\""
        + (room == null ? "" : room)
        + "\","
        + "\"hoursPerWeek\":"
        + hoursPerWeek
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LessonDto lessonDto = (LessonDto) o;
    return Double.compare(lessonDto.hoursPerWeek, hoursPerWeek) == 0
        && Objects.equals(teacherName, lessonNameSafe(lessonDto.teacherName))
        && Objects.equals(subjectName, lessonNameSafe(lessonDto.subjectName))
        && Objects.equals(className, lessonNameSafe(lessonDto.className))
        && Objects.equals(room, lessonNameSafe(lessonDto.room));
  }

  @Override
  public int hashCode() {
    return Objects.hash(teacherName, subjectName, className, room, hoursPerWeek);
  }

  private String lessonNameSafe(String s) {
    return s == null ? null : s;
  }
}
