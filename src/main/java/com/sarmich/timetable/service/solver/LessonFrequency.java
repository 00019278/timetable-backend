package com.sarmich.timetable.service.solver;

public enum LessonFrequency {
  WEEKLY(1), // Har hafta
  BI_WEEKLY(2), // 2 haftada bir
  TRI_WEEKLY(3); // 3 haftada bir

  public final int cycleLength; // Necha xil qatlam borligi

  LessonFrequency(int cycleLength) {
    this.cycleLength = cycleLength;
  }
}
