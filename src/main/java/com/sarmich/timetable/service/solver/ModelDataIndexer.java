package com.sarmich.timetable.service.solver;

import com.sarmich.timetable.model.response.*;
import com.sarmich.timetable.utils.Util;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ModelDataIndexer {

  /**
   * Solver uchun ma'lumotlarni indekslash. Endi bu metod API modelini (LessonResponse) emas, Solver
   * modelini (OrTLesson) qabul qiladi.
   *
   * @param lessons Solver uchun tayyorlangan yassi (flat) darslar ro'yxati.
   * @param company Kompaniya sozlamalari.
   * @param allRooms Barcha mavjud xonalar.
   * @return Indekslangan ma'lumotlar.
   */
  public ModelData indexData(
      List<OrTLesson> lessons, CompanyResponse company, List<RoomResponse> allRooms) {

    // 1. Class indexing (OrTLesson ichidagi classInfo dan olinadi)
    List<ClassResponse> uniqueClasses =
        lessons.stream().map(OrTLesson::classInfo).distinct().toList();

    HashMap<Integer, Integer> classIdToIndex = new HashMap<>();
    HashMap<Integer, ClassResponse> classIndexToObj = new HashMap<>();
    for (int i = 0; i < uniqueClasses.size(); i++) {
      classIdToIndex.put(uniqueClasses.get(i).id(), i);
      classIndexToObj.put(i, uniqueClasses.get(i));
    }

    // 2. Subject indexing
    List<SubjectResponse> uniqueSubjects =
        lessons.stream().map(OrTLesson::subject).distinct().toList();

    HashMap<Integer, Integer> subjectIdToIndex = new HashMap<>();
    HashMap<Integer, SubjectResponse> subjectIndexToObj = new HashMap<>();
    HashMap<Integer, Integer> subjectIndexToWeight = new HashMap<>();
    for (int i = 0; i < uniqueSubjects.size(); i++) {
      SubjectResponse subject = uniqueSubjects.get(i);
      subjectIdToIndex.put(subject.id(), i);
      subjectIndexToObj.put(i, subject);
      // Agar subject weight null bo'lsa, default 5 deb olamiz
      subjectIndexToWeight.put(i, Util.getNotNull(subject.weight(), 5));
    }

    // 3. Teacher indexing
    List<TeacherResponse> uniqueTeachers =
        lessons.stream().map(OrTLesson::teacher).distinct().toList();

    HashMap<Integer, Integer> teacherIdToIndex = new HashMap<>();
    HashMap<Integer, TeacherResponse> teacherIndexToObj = new HashMap<>();
    for (int i = 0; i < uniqueTeachers.size(); i++) {
      teacherIdToIndex.put(uniqueTeachers.get(i).id(), i);
      teacherIndexToObj.put(i, uniqueTeachers.get(i));
    }

    // 4. Room indexing (Bu o'zgarmaydi, chunki allRooms alohida keladi)
    List<RoomResponse> actualRooms = Util.getNotNull(allRooms, Collections.emptyList());
    HashMap<Integer, RoomResponse> roomIndexToObj = new HashMap<>();
    for (RoomResponse room : actualRooms) {
      roomIndexToObj.put(room.id(), room);
    }

    // 5. Hisoblangan parametrlar
    int days = company.daysOfWeek().size();
    int hoursPerDay = company.periods().stream().filter(p -> !p.isBreak()).mapToInt(p -> 1).sum();

    // 6. ModelData qurish (Builder ham OrTLesson qabul qiladigan bo'lishi kerak)
    return ModelData.builder()
        .lessons(lessons) // <-- O'zgargan joyi
        .company(company)
        .allRooms(actualRooms)
        .useRooms(!actualRooms.isEmpty())
        .uniqueClasses(uniqueClasses)
        .classIdToIndex(classIdToIndex)
        .classIndexToObj(classIndexToObj)
        .uniqueSubjects(uniqueSubjects)
        .subjectIdToIndex(subjectIdToIndex)
        .subjectIndexToObj(subjectIndexToObj)
        .subjectIndexToWeight(subjectIndexToWeight)
        .uniqueTeachers(uniqueTeachers)
        .teacherIdToIndex(teacherIdToIndex)
        .teacherIndexToObj(teacherIndexToObj)
        .roomIndexToObj(roomIndexToObj)
        .days(days)
        .hoursPerDay(hoursPerDay)
        .hoursCount(days * hoursPerDay)
        .build();
  }
}
