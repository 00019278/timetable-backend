package com.sarmich.timetable.mapper;

import java.time.Instant;

public class InstantMapper {

  public Long asLong(Instant instant) {
    return (instant != null) ? instant.toEpochMilli() : 0L;
  }

  public Instant asInstant(Long milli) {
    return (milli != null) ? Instant.ofEpochMilli(milli) : null;
  }
}
