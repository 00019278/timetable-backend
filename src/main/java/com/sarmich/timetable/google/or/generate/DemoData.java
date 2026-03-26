package com.sarmich.timetable.google.or.generate;

import com.sarmich.timetable.google.or.models.Lesson;
import com.sarmich.timetable.google.or.models.Subject;
import com.sarmich.timetable.google.or.models.Teacher;

import java.util.HashMap;
import java.util.List;

public class DemoData {
    public static List<Teacher> teacherList() {
        return List.of(
                new Teacher(1, "Yusupov B"),
                new Teacher(2, "Muhsinali "),
                new Teacher(3, "Abdullayeva L"),
                new Teacher(4, "Kenjayev I"),
                new Teacher(5, "Ziyodullayev R"),
                new Teacher(6, " Pirmatov U"),
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
                new Teacher(17, "Fayzullayev Sh"),
                new Teacher(18, "Berdiyev B"),
                new Teacher(19, "Po'latova Maqsuda"),
                new Teacher(20, "Behruz"),
                new Teacher(21, "Shavkat aka"),
                new Teacher(22, "Habiba"),
                new Teacher(23, "Yunusov H")
        );
    }

    public static List<Room> roomList() {
        return List.of(new Room(1, "1-xona"),
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
                new Subject(1, "Ona tili"),
                new Subject(2, "Rus tili"),
                new Subject(3, "Ingliz tili"),
                new Subject(4, "Algebra"),
                new Subject(5, "Fizika"),
                new Subject(6, "Kimyo"),
                new Subject(7, "O'zbekiston tarixi"),
                new Subject(8, "Geografiya"),
                new Subject(9, "Informatika"),
                new Subject(10, "Biologiya"),
                new Subject(11, "Chizmachilik"),
                new Subject(12, "Jismoniy tarbiya"),
                new Subject(13, "Huquq"),
                new Subject(14, "Texnologiya"),
                new Subject(15, "Adabiyot"),
                new Subject(16, "Tarbiya"),
                new Subject(17, "Geometriya"),
                new Subject(18, "Jahon tarixi"),
                new Subject(19, "Chyot"),
                new Subject(20, "Texnologiya"),
                new Subject(21, "Musiqa"),
                new Subject(22, "Tarbiya soat")
        );
    }

    public static List<Class> classList() {
        return List.of(new Class(1, "9-A"),
                new Class(2, "9-B"),
                new Class(3, "9-C"),
                new Class(4, "9-D"),
                new Class(5, "9-E"),
                new Class(6, "5-A"),
                new Class(7, "5-B"),
                new Class(8, "6-A"),
                new Class(8, "6-B"),
                new Class(8, "7-A"),
                new Class(8, "7-B"),
                new Class(8, "8-A"),
                new Class(8, "8-B")
        );
    }

    public static List<Lesson> lessonList() {
        List<Lesson> lessons = new java.util.ArrayList<>(List.of(
                new Lesson(1, 1, 11, 7, 1), // ozbekiston tarix
                new Lesson(2, 1, 11, 18, 1), //jahon tarix
                new Lesson(3, 1, 12, 11, 1),// chizmachilik
                new Lesson(4, 2, 10, 12, 1), // jismoniy tarbiya
                new Lesson(5, 3, 3, 3, 1), // ingliz tili
                new Lesson(6, 1, 9, 13, 1), // huquq
                new Lesson(7, 2, 13, 9, 1), // informatika
                new Lesson(8, 1, 1, 15, 1), // adabiyot
                new Lesson(9, 2, 7, 6, 1), // kimyo
                new Lesson(10, 1, 12, 14, 1), // texnologiya
                new Lesson(11, 2, 6, 5, 1), // fizika
                new Lesson(12, 2, 2, 2, 1), // rus tili
                new Lesson(13, 2, 1, 1, 1), // ona tili
                new Lesson(14, 1, 13, 8, 1), // geografiya
                new Lesson(15, 1, 9, 16, 1), // tarbiya
                new Lesson(16, 2, 8, 10, 1), // biologiya
                new Lesson(17, 2, 4, 4, 1), // algebra
                new Lesson(18, 1, 4, 17, 1),// geometriya

                // b sinf
                new Lesson(1, 1, 11, 7, 2), // ozbekiston tarix
                new Lesson(2, 1, 11, 18, 2), //jahon tarix
                new Lesson(3, 1, 12, 11, 2),// chizmachilik
                new Lesson(4, 2, 10, 12, 2), // jismoniy tarbiya
                new Lesson(5, 3, 3, 3, 2), // ingliz tili
                new Lesson(6, 1, 9, 13, 2), // huquq
                new Lesson(7, 2, 13, 9, 2), // informatika
                new Lesson(8, 1, 1, 15, 2), // adabiyot
                new Lesson(9, 2, 7, 6, 2), // kimyo
                new Lesson(10, 1, 12, 14, 2), // texnologiya
                new Lesson(11, 2, 6, 5, 2), // fizika
                new Lesson(12, 2, 2, 2, 2), // rus tili
                new Lesson(13, 2, 1, 1, 2), // ona tili
                new Lesson(14, 1, 13, 8, 2), // geografiya
                new Lesson(15, 1, 9, 16, 2), // tarbiya
                new Lesson(16, 2, 8, 10, 2), // biologiya
                new Lesson(17, 2, 4, 4, 2), // algebra
                new Lesson(18, 1, 4, 17, 2) // geometriya
                ,
                // c-sinf
                new Lesson(1, 1, 11, 7, 3), // ozbekiston tarix
                new Lesson(2, 1, 11, 18, 3), //jahon tarix
                new Lesson(3, 1, 12, 11, 3),// chizmachilik
                new Lesson(4, 2, 10, 12, 3), // jismoniy tarbiya
                new Lesson(5, 3, 3, 3, 3), // ingliz tili
                new Lesson(6, 1, 9, 13, 3), // huquq
                new Lesson(7, 2, 13, 9, 3), // informatika
                new Lesson(8, 1, 1, 15, 3), // adabiyot
                new Lesson(9, 2, 7, 6, 3), // kimyo
                new Lesson(10, 1, 12, 14, 3), // texnologiya
                new Lesson(11, 2, 6, 5, 3), // fizika
                new Lesson(12, 2, 2, 2, 3), // rus tili
                new Lesson(13, 2, 1, 1, 3), // ona tili
                new Lesson(14, 1, 13, 8, 3), // geografiya
                new Lesson(15, 1, 9, 16, 3), // tarbiya
                new Lesson(16, 2, 8, 10, 3), // biologiya
                new Lesson(17, 2, 4, 4, 3), // algebra
                new Lesson(18, 1, 4, 17, 3) // geometriya
                ,
                // c-sinf
                new Lesson(1, 1, 11, 7, 4), // ozbekiston tarix
                new Lesson(2, 1, 11, 18, 4), //jahon tarix
                new Lesson(3, 1, 12, 11, 4),// chizmachilik
                new Lesson(4, 2, 10, 12, 4), // jismoniy tarbiya
                new Lesson(5, 3, 3, 3, 4), // ingliz tili
                new Lesson(6, 1, 9, 13, 4), // huquq
                new Lesson(7, 2, 13, 9, 4), // informatika
                new Lesson(8, 1, 1, 15, 4), // adabiyot
                new Lesson(9, 2, 7, 6, 4), // kimyo
                new Lesson(10, 1, 12, 14, 4), // texnologiya
                new Lesson(11, 2, 6, 5, 4), // fizika
                new Lesson(12, 2, 2, 2, 4), // rus tili
                new Lesson(13, 2, 1, 1, 4), // ona tili
                new Lesson(14, 1, 13, 8, 4), // geografiya
                new Lesson(15, 1, 9, 16, 4), // tarbiya
                new Lesson(16, 2, 8, 10, 4), // biologiya
                new Lesson(17, 2, 4, 4, 4), // algebra
                new Lesson(18, 1, 4, 17, 4) // geometriya
                ,// c-sinf
                new Lesson(1, 1, 11, 7, 5), // ozbekiston tarix
                new Lesson(2, 1, 11, 18, 5), //jahon tarix
                new Lesson(3, 1, 12, 11, 5),// chizmachilik
                new Lesson(4, 2, 10, 12, 5), // jismoniy tarbiya
                new Lesson(5, 3, 3, 3, 5), // ingliz tili
                new Lesson(6, 1, 9, 13, 5), // huquq
                new Lesson(7, 2, 13, 9, 5), // informatika
                new Lesson(8, 1, 1, 15, 5), // adabiyot
                new Lesson(9, 2, 7, 6, 5), // kimyo
                new Lesson(10, 1, 12, 14, 5), // texnologiya
                new Lesson(11, 2, 6, 5, 5), // fizika
                new Lesson(12, 2, 2, 2, 5), // rus tili
                new Lesson(13, 2, 1, 1, 5), // ona tili
                new Lesson(14, 1, 13, 8, 5), // geografiya
                new Lesson(15, 1, 9, 16, 5), // tarbiya
                new Lesson(16, 2, 8, 10, 5), // biologiya
                new Lesson(17, 2, 4, 4, 5), // algebra
                new Lesson(18, 1, 4, 17, 5) // geometriya


        ));
        lessons.addAll(lesson5());
        lessons.addAll(lesson6());
        return lessons;
    }

    public static List<Lesson> lesson5() {
        return List.of(
                new Lesson(1, 2, 2, 2, 6), // ozbekiston tarix
                new Lesson(1, 2, 1, 1, 6), // ona tili
                new Lesson(1, 2, 10, 12, 6), // jismoniy tarbiya
                new Lesson(1, 2, 8, 10, 6),// biologiya
                new Lesson(1, 1, 9, 16, 6),// tarbiya
                new Lesson(1, 2, 12, 17, 6),// geografiya
                new Lesson(1, 2, 3, 3, 6),// inglish
                new Lesson(1, 2, 21, 20, 6),// texnologiya
                new Lesson(1, 2, 17, 18, 6),// tarix
                new Lesson(1, 2, 4, 4, 6),// matematika
                new Lesson(1, 2, 1, 15, 6),// adabiyot
                new Lesson(1, 2, 13, 9, 6),// informatika
                new Lesson(1, 2, 15, 21, 6)// musiqa
                ,
                new Lesson(1, 2, 2, 2, 7), // ozbekiston tarix
                new Lesson(1, 2, 1, 1, 7), // ona tili
                new Lesson(1, 2, 10, 12, 7), // jismoniy tarbiya
                new Lesson(1, 2, 8, 10, 7),// biologiya
                new Lesson(1, 1, 9, 16, 7),// tarbiya
                new Lesson(1, 2, 12, 17, 7),// geografiya
                new Lesson(1, 2, 3, 3, 7),// inglish
                new Lesson(1, 2, 21, 20, 7),// texnologiya
                new Lesson(1, 2, 17, 18, 7),// tarix
                new Lesson(1, 2, 4, 4, 7),// matematika
                new Lesson(1, 2, 1, 15, 7),// adabiyot
                new Lesson(1, 2, 13, 9, 7),// informatika
                new Lesson(1, 2, 15, 21, 7)// musiqa
        );
    }

    public static List<Lesson> lesson6() {
        return List.of(
                new Lesson(1, 2, 2, 2, 8), // ozbekiston tarix
                new Lesson(1, 2, 1, 1, 8), // ona tili
                new Lesson(1, 2, 10, 12, 8), // jismoniy tarbiya
                new Lesson(1, 2, 8, 10, 8),// biologiya
                new Lesson(1, 1, 9, 16, 8),// tarbiya
                new Lesson(1, 2, 12, 17, 8),// geografiya
                new Lesson(1, 2, 3, 3, 8),// inglish
                new Lesson(1, 2, 21, 20, 8),// texnologiya
                new Lesson(1, 2, 17, 18, 8),// tarix
                new Lesson(1, 2, 4, 4, 8),// matematika
                new Lesson(1, 2, 1, 15, 8),// adabiyot
                new Lesson(1, 2, 13, 9, 8),// informatika
                new Lesson(1, 2, 15, 21, 8),// musiqa


                new Lesson(1, 2, 2, 2, 9), // ozbekiston tarix
                new Lesson(1, 2, 1, 1, 9), // ona tili
                new Lesson(1, 2, 10, 12, 9), // jismoniy tarbiya
                new Lesson(1, 2, 9, 10, 9),// biologiya
                new Lesson(1, 1, 8, 16, 9),// tarbiya
                new Lesson(1, 2, 12, 17, 9),// geografiya
                new Lesson(1, 2, 3, 3, 9),// inglish
                new Lesson(1, 2, 21, 20, 9),// texnologiya
                new Lesson(1, 2, 17, 18, 9),// tarix
                new Lesson(1, 2, 4, 4, 9),// matematika
                new Lesson(1, 2, 1, 15, 9),// adabiyot
                new Lesson(1, 2, 13, 9, 9),// informatika
                new Lesson(1, 2, 15, 21, 9)// musiqa
        );
    }
    public static HashMap<Integer, Subject> getSubjectMap(){
        HashMap<Integer,Subject> map = new HashMap<>();
        subjectList().forEach(s->map.put(s.getId(),s));
        return map;
    }
    public static HashMap<Integer, Teacher> getTeacherMap(){
        HashMap<Integer,Teacher> map = new HashMap<>();
        teacherList().forEach(s->map.put(s.getId(),s));
        return map;
    }
}
