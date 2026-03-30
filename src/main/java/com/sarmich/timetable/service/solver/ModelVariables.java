package com.sarmich.timetable.service.solver;

import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.IntVar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class ModelVariables {
  // 1. Asosiy qaror o'zgaruvchilari: "Shu vaqtda dars bormi?"
  // Key formati: "c{class}_t{teacher}_s{subject}_h{hour}_r{room}_p{period}_l{lessonId}"
  private final Map<String, BoolVar> assignmentVars = new HashMap<>();

  // 2. Hafta o'zgaruvchilari (Bi-weekly/Tri-weekly uchun)

  // A) O'zgaruvchi nomi (Key) bo'yicha bog'langan hafta (Constraintlar uchun)
  private final Map<String, IntVar> lessonWeekVars = new HashMap<>();

  // B) Lesson ID bo'yicha bog'langan hafta (VariableFactory uchun)
  private final Map<Integer, IntVar> lessonIdToWeekVar = new HashMap<>();

  // C) YANGI: SyncID bo'yicha bog'langan hafta (Parallel darslar uchun)
  // Agar darsda syncId bo'lsa, biz hafta o'zgaruvchisini shu yerdan olamiz/saqlaymiz.
  // Bu "Group A" va "Group B" ning haftasi (A/B) bir xil bo'lishini ta'minlaydi.
  private final Map<String, IntVar> syncIdToWeekVar = new HashMap<>();

  // 3. Cheklovlarni tezlashtirish uchun guruhlangan ro'yxatlar (Keshlar)

  // O'qituvchi va Soat bo'yicha
  private final Map<String, List<BoolVar>> lessonsByTeacherHour = new HashMap<>();

  // Sinf va Soat bo'yicha
  private final Map<String, List<BoolVar>> lessonsByClassHour = new HashMap<>();

  // Xona va Soat bo'yicha
  private final Map<String, List<BoolVar>> lessonsByRoomHour = new HashMap<>();

  // Sinf, O'qituvchi va Fan bo'yicha
  private final Map<String, List<BoolVar>> lessonsByClassTeacherSubject = new HashMap<>();

  // 4. YANGI: Ketma-ketlik (Sequential) cheklovi uchun maxsus kesh
  // Key formati: "{lessonId}_{startHour}" (Masalan: "105_2" -> IDsi 105 dars 2-soatda boshlanyapti)
  // Value: List<BoolVar> (Chunki bir xil dars, bir xil soatda, lekin har xil xonada bo'lishi
  // mumkin)
  private final Map<String, List<BoolVar>> lessonStartVars = new HashMap<>();
}
