package com.sarmich.timetable.google.or.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoDataClone {
  public static List<Teacher> teacherList() {
    return List.of(
        new Teacher(1, "Yusupov B"),
        new Teacher(2, "Muhsinali "),
        new Teacher(3, "Abdullayeva L"),
        new Teacher(4, "Kenjayev I"),
        new Teacher(5, "Ziyodullayev R"),
        new Teacher(6, "Pirmatov U"),
        new Teacher(7, "Dodojonov A"),
        new Teacher(8, "Otajonov J"),
        new Teacher(9, "Abduqodirov D"),
        new Teacher(10, "Ostanaqulov N"),
        new Teacher(11, "Ashurov N"),
        new Teacher(12, "Abdullayev R"),
        new Teacher(13, "Qo'ldoshev T"),
        new Teacher(13, "Sultomurod aka"),
        new Teacher(14, "Najmiddin aka"),
        new Teacher(15, "Qayumov H"),
        new Teacher(16, "Fayzullayev N"),
        new Teacher(17, "Fayzullayev Sh"), // 1-a
        new Teacher(18, "Berdiyev B"),
        new Teacher(19, "Po'latova Maqsuda"),
        new Teacher(20, "Behruz"),
        new Teacher(21, "Shavkat aka"),
        new Teacher(22, "Habiba"),
        new Teacher(23, "Sayfullayev D"),
        new Teacher(24, "Yunusov H"));
  }

  public static List<Room> roomList() {
    return List.of(
        new Room(1, "1-xona"),
        new Room(2, "2-xona"),
        new Room(3, "3-xona"),
        new Room(4, "4-xona"),
        new Room(5, "5-xona"),
        new Room(6, "6-xona"),
        new Room(7, "7-xona"),
        new Room(8, "8-xona"),
        new Room(9, "9-xona"),
        new Room(10, "10-xona"),
        new Room(11, "11-xona"));
  }

  public static List<Subject> subjectList() {
    return List.of(
        new Subject(1, "Alifbe"),
        new Subject(2, "Yozuv"),
        new Subject(3, "O‘qish"),
        new Subject(4, "O‘qish savodxonligi"),
        new Subject(5, "Ona tili"),
        new Subject(6, "Rus tili"),
        new Subject(7, "Ingliz tili"),
        new Subject(8, "Matematika"),
        new Subject(9, "Algebra"),
        new Subject(10, "Geometriya"),
        new Subject(11, "Fizika"),
        new Subject(12, "Kimyo"),
        new Subject(13, "Biologiya"),
        new Subject(14, "Geografiya"),
        new Subject(15, "Astronomiya"),
        new Subject(16, "Science"),
        new Subject(17, "Tabiatshunoslik"),
        new Subject(18, "Tarbiya"),
        new Subject(19, "Sinf soati"),
        new Subject(20, "O‘zbekiston tarixi"),
        new Subject(21, "Jahon tarixi"),
        new Subject(22, "Tarix"),
        new Subject(23, "Tarixdan hikoyalar"),
        new Subject(24, "Huquq"),
        new Subject(25, "Iqtisod"),
        new Subject(26, "Tadbirkorlik"),
        new Subject(27, "Adabiyot"),
        new Subject(28, "Musiqa"),
        new Subject(29, "Tasviriy san’at"),
        new Subject(30, "Chizmachilik"),
        new Subject(31, "Texnologiya"),
        new Subject(32, "EHM"),
        new Subject(33, "Jismoniy tarbiya"));
  }

  public static List<Class> classList() {
    return List.of(
        new Class(1, "1-A"),
        new Class(2, "1-B"),
        new Class(3, "2-A"),
        new Class(4, "3-A"),
        new Class(5, "3-B"),
        new Class(6, "4-A"),
        new Class(7, "4-B"),
        new Class(8, "5-A"),
        new Class(9, "6-A"),
        new Class(10, "7-A"),
        new Class(11, "7-B"),
        new Class(12, "8-A"),
        new Class(13, "8-B"),
        new Class(14, "9-A"),
        new Class(15, "9-B"),
        new Class(16, "10-A"),
        new Class(17, "11-A"),
        new Class(18, "11-B"));
  }

  public static Map<Integer, Class> classMap() {
    Map<Integer, Class> map = new HashMap<>();
    classList().forEach(c -> map.put(c.getId(), c));
    return map;
  }

  public static List<Lesson> lessonList() {
    List<Lesson> lessons = new ArrayList<>(firstAClassLessons());
    lessons.addAll(firstBClassLessons());
    return lessons;
  }

  private static List<Lesson> firstAClassLessons() {
    return List.of(
        new Lesson(1, 2, 10, 33, 1), // jismoniy tarbiya
        new Lesson(1, 4, 17, 1, 1), // alifbe
        new Lesson(1, 2, 17, 2, 1), // yozuv
        new Lesson(1, 5, 17, 8, 1), // matematika
        new Lesson(1, 2, 13, 7, 1), // ingliz
        new Lesson(1, 1, 17, 18, 1), // tarbiya
        new Lesson(1, 1, 15, 28, 1), // musiqa
        new Lesson(1, 1, 17, 16, 1), // science
        new Lesson(1, 1, 17, 19, 1), // sinf soati
        new Lesson(1, 1, 17, 29, 1)); // tas san
  }

  private static List<Lesson> firstBClassLessons() {
    return List.of(
        new Lesson(1, 2, 10, 33, 1), // jismoniy tarbiya
        new Lesson(1, 4, 23, 1, 1), // alifbe
        new Lesson(1, 2, 23, 2, 1), // yozuv
        new Lesson(1, 5, 23, 8, 1), // matematika
        new Lesson(1, 2, 13, 7, 1), // ingliz
        new Lesson(1, 1, 23, 18, 1), // tarbiya
        new Lesson(1, 1, 15, 28, 1), // musiqa
        new Lesson(1, 1, 23, 16, 1), // science
        new Lesson(1, 1, 23, 19, 1), // sinf soati
        new Lesson(1, 1, 23, 29, 1)); // tas san
  }

  public static List<Lesson> lesson5() {
    return List.of(
        new Lesson(1, 2, 2, 2, 6), // ozbekiston tarix
        new Lesson(1, 2, 1, 1, 6), // ona tili
        new Lesson(1, 2, 10, 12, 6), // jismoniy tarbiya
        new Lesson(1, 2, 8, 10, 6), // biologiya
        new Lesson(1, 1, 9, 16, 6), // tarbiya
        new Lesson(1, 2, 12, 17, 6), // geografiya
        new Lesson(1, 2, 3, 3, 6), // inglish
        new Lesson(1, 2, 21, 20, 6), // texnologiya
        new Lesson(1, 2, 17, 18, 6), // tarix
        new Lesson(1, 2, 4, 4, 6), // matematika
        new Lesson(1, 2, 1, 15, 6), // adabiyot
        new Lesson(1, 2, 13, 9, 6), // informatika
        new Lesson(1, 2, 15, 21, 6) // musiqa
        ,
        new Lesson(1, 2, 2, 2, 7), // ozbekiston tarix
        new Lesson(1, 2, 1, 1, 7), // ona tili
        new Lesson(1, 2, 10, 12, 7), // jismoniy tarbiya
        new Lesson(1, 2, 8, 10, 7), // biologiya
        new Lesson(1, 1, 9, 16, 7), // tarbiya
        new Lesson(1, 2, 12, 17, 7), // geografiya
        new Lesson(1, 2, 3, 3, 7), // inglish
        new Lesson(1, 2, 21, 20, 7), // texnologiya
        new Lesson(1, 2, 17, 18, 7), // tarix
        new Lesson(1, 2, 4, 4, 7), // matematika
        new Lesson(1, 2, 1, 15, 7), // adabiyot
        new Lesson(1, 2, 13, 9, 7), // informatika
        new Lesson(1, 2, 15, 21, 7) // musiqa
        );
  }

  public static List<Lesson> lesson6() {
    return List.of(
        new Lesson(1, 2, 2, 2, 8), // ozbekiston tarix
        new Lesson(1, 2, 1, 1, 8), // ona tili
        new Lesson(1, 2, 10, 12, 8), // jismoniy tarbiya
        new Lesson(1, 2, 8, 10, 8), // biologiya
        new Lesson(1, 1, 9, 16, 8), // tarbiya
        new Lesson(1, 2, 12, 17, 8), // geografiya
        new Lesson(1, 2, 3, 3, 8), // inglish
        new Lesson(1, 2, 21, 20, 8), // texnologiya
        new Lesson(1, 2, 17, 18, 8), // tarix
        new Lesson(1, 2, 4, 4, 8), // matematika
        new Lesson(1, 2, 1, 15, 8), // adabiyot
        new Lesson(1, 2, 13, 9, 8), // informatika
        new Lesson(1, 2, 15, 21, 8), // musiqa
        new Lesson(1, 2, 2, 2, 9), // ozbekiston tarix
        new Lesson(1, 2, 1, 1, 9), // ona tili
        new Lesson(1, 2, 10, 12, 9), // jismoniy tarbiya
        new Lesson(1, 2, 9, 10, 9), // biologiya
        new Lesson(1, 1, 8, 16, 9), // tarbiya
        new Lesson(1, 2, 12, 17, 9), // geografiya
        new Lesson(1, 2, 3, 3, 9), // inglish
        new Lesson(1, 2, 21, 20, 9), // texnologiya
        new Lesson(1, 2, 17, 18, 9), // tarix
        new Lesson(1, 2, 4, 4, 9), // matematika
        new Lesson(1, 2, 1, 15, 9), // adabiyot
        new Lesson(1, 2, 13, 9, 9), // informatika
        new Lesson(1, 2, 15, 21, 9) // musiqa
        );
  }

  public static HashMap<Integer, Subject> getSubjectMap() {
    HashMap<Integer, Subject> map = new HashMap<>();
    subjectList().forEach(s -> map.put(s.getId(), s));
    return map;
  }

  public static HashMap<Integer, Teacher> getTeacherMap() {
    HashMap<Integer, Teacher> map = new HashMap<>();
    teacherList().forEach(s -> map.put(s.getId(), s));
    return map;
  }
}
