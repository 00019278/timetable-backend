package com.sarmich.timetable.model;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Objects;

public record TimeSlot(DayOfWeek dayOfWeek, List<Integer> lessons) {

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    TimeSlot timeSlot = (TimeSlot) o;
    return dayOfWeek == timeSlot.dayOfWeek;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(dayOfWeek);
  }
}
