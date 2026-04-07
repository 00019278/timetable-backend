package com.sarmich.timetable.service.solver;

import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class ModelVariables {
  // 1. Asosiy qaror o'zgaruvchilari (Interval-based)
  // Key: Lesson ID (Internal Solver ID or OrTLesson ID)
  private final Map<Integer, IntVar> lessonStartVars = new HashMap<>();
  private final Map<Integer, IntVar> lessonEndVars =
      new HashMap<>(); // Optional if needed explicitly
  private final Map<Integer, IntervalVar> lessonIntervalVars = new HashMap<>();

  // 2. Hafta o'zgaruvchilari (Bi-weekly logic)
  // Key: Lesson ID -> WeekIntVar (0 or 1)
  private final Map<Integer, IntVar> lessonWeekVars = new HashMap<>();

  // Sync Groups uchun map (SyncId -> WeekVar)
  private final Map<String, IntVar> syncIdToWeekVar = new HashMap<>();

  // 3. Resurslar uchun intervallar ro'yxati (Resource constraintlari uchun)
  // Week A
  private final Map<Integer, List<IntervalVar>> teacherIntervalsA = new HashMap<>();
  private final Map<Integer, List<IntervalVar>> classIntervalsA = new HashMap<>();
  private final Map<Integer, List<IntervalVar>> roomIntervalsA = new HashMap<>();

  // Week B
  private final Map<Integer, List<IntervalVar>> teacherIntervalsB = new HashMap<>();
  private final Map<Integer, List<IntervalVar>> classIntervalsB = new HashMap<>();
  private final Map<Integer, List<IntervalVar>> roomIntervalsB = new HashMap<>();

  // 4. Guruhlar uchun intervallar (Separated from Class for independent Subgroup
  // scheduling)
  // Key: Group ID -> List of Intervals
  private final Map<Integer, List<IntervalVar>> groupIntervalsA = new HashMap<>();
  private final Map<Integer, List<IntervalVar>> groupIntervalsB = new HashMap<>();

  // Helper accessors
  public void addTeacherIntervalA(Integer teacherId, IntervalVar interval) {
    teacherIntervalsA.computeIfAbsent(teacherId, k -> new ArrayList<>()).add(interval);
  }

  public void addTeacherIntervalB(Integer teacherId, IntervalVar interval) {
    teacherIntervalsB.computeIfAbsent(teacherId, k -> new ArrayList<>()).add(interval);
  }

  public void addClassIntervalA(Integer classId, IntervalVar interval) {
    classIntervalsA.computeIfAbsent(classId, k -> new ArrayList<>()).add(interval);
  }

  public void addClassIntervalB(Integer classId, IntervalVar interval) {
    classIntervalsB.computeIfAbsent(classId, k -> new ArrayList<>()).add(interval);
  }

  public void addRoomIntervalA(Integer roomId, IntervalVar interval) {
    roomIntervalsA.computeIfAbsent(roomId, k -> new ArrayList<>()).add(interval);
  }

  public void addRoomIntervalB(Integer roomId, IntervalVar interval) {
    roomIntervalsB.computeIfAbsent(roomId, k -> new ArrayList<>()).add(interval);
  }

  public void addGroupIntervalA(Integer groupId, IntervalVar interval) {
    groupIntervalsA.computeIfAbsent(groupId, k -> new ArrayList<>()).add(interval);
  }

  public void addGroupIntervalB(Integer groupId, IntervalVar interval) {
    groupIntervalsB.computeIfAbsent(groupId, k -> new ArrayList<>()).add(interval);
  }
}
