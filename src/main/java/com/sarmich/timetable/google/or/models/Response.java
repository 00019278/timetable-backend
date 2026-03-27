package com.sarmich.timetable.google.or.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Response {
  DayOfWeek day;
  Integer hour;
  Teacher teacher;
  Subject subject;
  Class classObj;
  Integer roomId;
}
