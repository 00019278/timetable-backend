package com.sarmich.timetable.service.solver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleScore {
  private int totalPenaltyScore; // Umumiy jarima bali
  private int teacherGaps; // O'qituvchilardagi oynalar soni
  private int classGaps; // Sinflardagi oynalar soni
  private int unscheduledLessons; // Qo'yilmay qolgan darslar soni (soat hisobida)

  // Foydalanuvchiga ko'rsatish uchun qulay format
  public String getSummary() {
    return String.format(
        "Score: %d | Unscheduled: %d | Teacher Gaps: %d | Class Gaps: %d",
        totalPenaltyScore, unscheduledLessons, teacherGaps, classGaps);
  }
}
