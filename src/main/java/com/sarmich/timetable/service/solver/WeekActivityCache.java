package com.sarmich.timetable.service.solver;

import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;

/** Stubbed for Interval refactor. */
public class WeekActivityCache {

  public WeekActivityCache(CpModel model, ModelVariables vars) {}

  public BoolVar getIsWeek0Var(BoolVar lessonVar) {
    return null;
  }

  public BoolVar getActiveInWeek0(BoolVar lessonVar) {
    return null;
  }

  public BoolVar getActiveInWeek1(BoolVar lessonVar) {
    return null;
  }

  public int getCacheSize() {
    return 0;
  }
}
