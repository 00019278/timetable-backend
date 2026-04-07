package com.sarmich.timetable.model;

import com.sarmich.timetable.model.response.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimetableGroupDetail {
  private Integer lessonId;
  private Integer subjectId;
  private Integer teacherId;
  private Integer roomId;
  private Integer groupId; // Agar null bo'lsa -> Butun sinf
  // --- YANGI QO'SHILDI ---
  // Har bir guruh darsining o'z "pasporti" (original ma'lumoti) bo'ladi
  private LessonResponse originalLessonData;
}
