package com.sarmich.timetable.service.solver;

import com.sarmich.timetable.model.response.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ModelData {
  // --- Kiruvchi Ma'lumotlar ---
  private final List<OrTLesson> lessons;
  private final CompanyResponse company;

  // Xonalarga oid yangi ma'lumotlar
  @Builder.Default // Agar quruvchida berilmasa, bo'sh ro'yxat bo'lsin
  private final List<RoomResponse> allRooms = Collections.emptyList();
  private final boolean useRooms;

  // --- Indekslangan Ma'lumotlar ---
  private final List<ClassResponse> uniqueClasses;
  private final HashMap<Integer, Integer> classIdToIndex;
  private final HashMap<Integer, ClassResponse> classIndexToObj;

  private final List<SubjectResponse> uniqueSubjects;
  private final HashMap<Integer, Integer> subjectIdToIndex;
  private final HashMap<Integer, SubjectResponse> subjectIndexToObj;
  private final HashMap<Integer, Integer> subjectIndexToWeight;

  private final List<TeacherResponse> uniqueTeachers;
  private final HashMap<Integer, Integer> teacherIdToIndex;
  private final HashMap<Integer, TeacherResponse> teacherIndexToObj;

  // Xonalarni ID bo'yicha tez topish uchun yangi xarita
  private final HashMap<Integer, RoomResponse> roomIndexToObj;

  // --- Hisoblangan Parametrlar ---
  private final int days;
  private final int hoursPerDay;
  private final int hoursCount;

  // Eskirgan maydon, lekin orqaga moslik uchun qoldirish mumkin. Yaxshisi olib tashlash.
  // private final Integer roomCount;
}
