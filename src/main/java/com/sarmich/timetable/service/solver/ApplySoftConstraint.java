package com.sarmich.timetable.service.solver;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplySoftConstraint {

  // Global switch: Yumshoq cheklovlarni umuman yoqish/o'chirish
  @Builder.Default private Boolean applySoftConstraint = true;

  // =================================================================================
  // 1-QATLAM: KRITIK (CRITICAL)
  // Maqsad: Dars jadvalga sig'may qolishining oldini olish.
  // Bu jarima boshqalardan keskin katta bo'lishi shart.
  // =================================================================================
  @Builder.Default private Boolean applyUnScheduledLessons = true;

  @Builder.Default private Integer applyUnScheduledLessonsPenalty = 100_000;

  // =================================================================================
  // 2-QATLAM: YUQORI (HIGH)
  // Maqsad: O'qituvchilarning vaqtini tejash (Oynalarni yo'qotish).
  // O'qituvchi rozi bo'lishi uchun bu yuqori turishi kerak.
  // =================================================================================
  @Builder.Default private Boolean applyContinuityPenaltyTeacher = true;

  @Builder.Default private Integer applyContinuityPenaltyTeacherPenalty = 100;

  // =================================================================================
  // 3-QATLAM: O'RTA (MEDIUM)
  // Maqsad: O'quvchilarning kunlik tartibi (Sinf oynalari).
  // O'quvchilar kutib turishi mumkin, lekin baribir "window" bo'lmagani yaxshi.
  // =================================================================================
  @Builder.Default private Boolean applyContinuityPenaltyClass = true;

  @Builder.Default private Integer applyContinuityPenaltyClassPenalty = 50;

  // =================================================================================
  // 4-QATLAM: PAST (LOW)
  // Maqsad: Kunlik yuklama va fanlar taqsimoti.
  // Bir kunda 2 ta matematika bo'lib qolsa yoki bir kun 4, bir kun 6 soat bo'lsa - bu fojia emas.
  // =================================================================================

  // Yuklama Balansi (Balanced Weighted Load)
  @Builder.Default private Boolean applyBalancedLoad = true;

  @Builder.Default private Integer applyBalancedLoadPenalty = 10;

  // Fanlar Taqsimoti (Daily Subject Distribution - bir kunda 1 fandan ko'p bo'lmasin)
  // Bu balansdan ko'ra muhimroq, shuning uchun 20.
  @Builder.Default private Boolean applyDailySubjectDistribution = true;

  @Builder.Default private Integer applyDailySubjectDistributionPenalty = 20;

  // =================================================================================
  // 5-QATLAM: MIKRO (MINOR)
  // Maqsad: Estetika va A/B hafta balansi.
  // =================================================================================
  @Builder.Default private Boolean applyWeekParity = true;

  @Builder.Default private Integer applyWeekParityPenalty = 5;
}
