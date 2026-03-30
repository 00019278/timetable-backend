package com.sarmich.timetable.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolverResult {
  private List<TimetableSlotResponse> scheduledSlots; // Tuzilgan darslar jadvali
  private List<UnscheduledLesson> unscheduledLessons; // Sig'magan darslar ro'yxati
}
