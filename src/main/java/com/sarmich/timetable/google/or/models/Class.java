package com.sarmich.timetable.google.or.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Class {
  Integer id;
  String name;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Class aClass = (Class) o;
    return Objects.equals(id, aClass.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
