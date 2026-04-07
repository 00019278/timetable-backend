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
public class UnscheduledLesson {
  private Integer classId;
  private Integer teacherId;
  private Integer subjectId;
  private List<Integer> roomIds;
  private int requiredCount; // Nechta soat kerak edi
  private int scheduledCount; // Nechta soat aslida qo'yildi
  private int missingCount; // Nechta soat sig'madi
}
