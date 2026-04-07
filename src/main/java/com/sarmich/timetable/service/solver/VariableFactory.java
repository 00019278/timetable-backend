package com.sarmich.timetable.service.solver;

import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.util.Domain;
import com.sarmich.timetable.model.response.*;
import com.sarmich.timetable.utils.Util;
import java.util.*;
import java.util.stream.IntStream;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class VariableFactory {

  public ModelVariables createVariables(CpModel model, ModelData data) {
    ModelVariables variables = new ModelVariables();
    int totalHours = data.getHoursCount();
    int hoursPerDay = data.getHoursPerDay();

    // 1. Convert Availabilities to Fixed Intervals (TimeOffs)
    processFixedIntervals(model, variables, data);

    // 2. Create Lesson Variables
    for (OrTLesson lesson : data.getLessons()) {
      final int duration = Util.getNotNull(lesson.period(), 1);
      LessonFrequency freq = Util.getNotNull(lesson.frequency(), LessonFrequency.WEEKLY);

      Integer cIdx = data.getClassIdToIndex().get(lesson.classInfo().id());
      Integer tIdx = data.getTeacherIdToIndex().get(lesson.teacher().id());
      Integer sIdx = data.getSubjectIdToIndex().get(lesson.subject().id());

      if (cIdx == null || tIdx == null || sIdx == null) {
        log.warn("Missing index for lesson ID: {}", lesson.id());
        continue;
      }

      TeacherResponse teacherObj = data.getTeacherIndexToObj().get(tIdx);
      SubjectResponse subjectObj = data.getSubjectIndexToObj().get(sIdx);

      // 2.1 Domain Calculation (handling Subject TimeOffs and Day Boundaries)
      Domain allowedStarts = calculateAllowedStarts(subjectObj, totalHours, hoursPerDay, duration);

      // 2.2 Create Start Var
      String startName = "start_l" + lesson.id();
      IntVar startVar = model.newIntVarFromDomain(allowedStarts, startName);
      variables.getLessonStartVars().put(lesson.id(), startVar);

      // 2.3 Create End Var (Redundant but explicit)
      // End = Start + Duration. We don't necessarily need a variable if we use Fixed
      // Duration intervals
      // but OrTools accepts LinearExpr for size/end. We'll use fixed size int.

      // 2.4 Handle Week Parity (Bi-Weekly)
      boolean isBiWeekly = (freq == LessonFrequency.BI_WEEKLY);

      BoolVar weekALit = null; // True if occurs in Week A
      if (isBiWeekly) {
        IntVar weekVar = getOrCreateWeekVar(model, variables, lesson, freq);
        weekALit = model.newBoolVar("is_week_A_l" + lesson.id());
        // weekVar == 0 <-> weekALit is True
        model.addEquality(weekVar, 0).onlyEnforceIf(weekALit);
        model.addEquality(weekVar, 1).onlyEnforceIf(weekALit.not());
      }

      // 2.5 Create Interval(s)
      String intervalName = "interval_l" + lesson.id();

      if (!isBiWeekly) {
        // Weekly: Occurs in BOTH A and B (conceptually) or simply on the timeline.
        // Since we use separate lists for A and B NoOverlap, we must add this interval
        // to BOTH.
        IntervalVar interval = model.newFixedSizeIntervalVar(startVar, duration, intervalName);
        variables.getLessonIntervalVars().put(lesson.id(), interval);

        // Add to Teacher resource lists (Both A and B)
        variables.addTeacherIntervalA(tIdx, interval);
        variables.addTeacherIntervalB(tIdx, interval);

        // Class & Group Logic
        if (lesson.group() != null) {
          Integer gIdx = lesson.group().id();
          variables.addGroupIntervalA(gIdx, interval);
          variables.addGroupIntervalB(gIdx, interval);
          // Do NOT add to ClassIntervalsA/B.
          // Instead, we will enforce NoOverlap(Group + Class) in HardConstraints.
        } else {
          variables.addClassIntervalA(cIdx, interval);
          variables.addClassIntervalB(cIdx, interval);
        }

        if (data.isUseRooms() && lesson.rooms() != null && !lesson.rooms().isEmpty()) {
          // Xonalar bilan ishlash:
          // 1. Agar faqat bitta xona bo'lsa - to'g'ridan-to'g'ri intervalga qo'shamiz
          // 2. Agar bir nechta xona bo'lsa - birinchisini (eng yuqori prioritetli)
          // tanlaymiz
          // (Post-processing da boshqa xona tanlash mumkin, lekin NoOverlap uchun
          // kamida bitta xonani band qilishimiz kerak)

          // Birinchi xonani olamiz (prioritet bo'yicha eng yaxshisi)
          RoomResponse primaryRoom = lesson.rooms().get(0);
          Integer roomId = primaryRoom.id();

          // Xona intervalini qo'shamiz
          variables.addRoomIntervalA(roomId, interval);
          variables.addRoomIntervalB(roomId, interval);
        }
      } else {
        // Bi-Weekly: Occurs in A OR B
        // Interval A: Present if weekALit
        IntervalVar intervalA =
            model.newOptionalFixedSizeIntervalVar(
                startVar, duration, weekALit, intervalName + "_A");

        // Interval B: Present if !weekALit
        IntervalVar intervalB =
            model.newOptionalFixedSizeIntervalVar(
                startVar, duration, weekALit.not(), intervalName + "_B");

        // Note: We don't put these into 'lessonIntervalVars' as a single entry easily.
        // Maybe just put A? Or maintain a map for debug?
        variables.getLessonIntervalVars().put(lesson.id(), intervalA); // Just storing one for ref

        variables.addTeacherIntervalA(tIdx, intervalA);
        variables.addTeacherIntervalB(tIdx, intervalB);

        if (lesson.group() != null) {
          Integer gIdx = lesson.group().id();
          variables.addGroupIntervalA(gIdx, intervalA);
          variables.addGroupIntervalB(gIdx, intervalB);
        } else {
          variables.addClassIntervalA(cIdx, intervalA);
          variables.addClassIntervalB(cIdx, intervalB);
        }

        // Bi-Weekly darslar uchun ham xona intervallarini qo'shamiz
        if (data.isUseRooms() && lesson.rooms() != null && !lesson.rooms().isEmpty()) {
          RoomResponse primaryRoom = lesson.rooms().get(0);
          Integer roomId = primaryRoom.id();
          variables.addRoomIntervalA(roomId, intervalA);
          variables.addRoomIntervalB(roomId, intervalB);
        }
      }
    }

    return variables;
  }

  private void processFixedIntervals(CpModel model, ModelVariables vars, ModelData data) {
    // 1. Teachers
    for (int tIdx = 0; tIdx < data.getUniqueTeachers().size(); tIdx++) {
      TeacherResponse t = data.getTeacherIndexToObj().get(tIdx);
      List<IntervalVar> gaps =
          createGapIntervals(
              model, t.availabilities(), data.getHoursPerDay(), data.getDays(), "teacher_" + tIdx);
      for (IntervalVar gap : gaps) {
        vars.addTeacherIntervalA(tIdx, gap);
        vars.addTeacherIntervalB(tIdx, gap);
      }
    }

    // 2. Classes
    for (int cIdx = 0; cIdx < data.getUniqueClasses().size(); cIdx++) {
      ClassResponse c = data.getClassIndexToObj().get(cIdx);
      List<IntervalVar> gaps =
          createGapIntervals(
              model, c.availabilities(), data.getHoursPerDay(), data.getDays(), "class_" + cIdx);
      for (IntervalVar gap : gaps) {
        vars.addClassIntervalA(cIdx, gap);
        vars.addClassIntervalB(cIdx, gap);
      }
    }

    // 3. Rooms (If needed, check room availabilities)
    if (data.isUseRooms()) {
      // Handle if rooms have availabilities
      for (Map.Entry<Integer, RoomResponse> entry : data.getRoomIndexToObj().entrySet()) {
        RoomResponse room = entry.getValue();
        List<IntervalVar> gaps =
            createGapIntervals(
                model,
                room.availabilities(),
                data.getHoursPerDay(),
                data.getDays(),
                "room_" + room.id());
        for (IntervalVar gap : gaps) {
          vars.addRoomIntervalA(room.id(), gap);
          vars.addRoomIntervalB(room.id(), gap);
        }
      }
    }
  }

  // Creates FixedIntervals for periods where the resource is NOT available.
  // "Crucial: Group consecutive TimeOff hours into a single Fixed Interval"
  private List<IntervalVar> createGapIntervals(
      CpModel model,
      List<com.sarmich.timetable.model.TimeSlot> availabilities,
      int hpd,
      int days,
      String prefix) {
    List<IntervalVar> intervals = new ArrayList<>();

    // Fix: Treat null OR EMPTY list as "Full Availability" (No Gaps).
    // Previously, empty list resulted in 'isAvailable' staying all false, causing a
    // 100% block.
    if (availabilities == null || availabilities.isEmpty()) return intervals;

    // Convert availability slots to a boolean array of "Is Available"
    boolean[] isAvailable = new boolean[days * hpd]; // default false
    // Fill available slots
    for (com.sarmich.timetable.model.TimeSlot slot : availabilities) {
      // slot.dayOfWeek (1-7), slot.lessons (1-based indexes)
      int dIndex = slot.dayOfWeek().getValue() - 1;
      if (dIndex >= 0 && dIndex < days) {
        for (Integer lessonIdx : slot.lessons()) {
          int hIndex = lessonIdx - 1;
          if (hIndex >= 0 && hIndex < hpd) {
            isAvailable[dIndex * hpd + hIndex] = true;
          }
        }
      }
    }

    // Find gaps (sequences of false)
    int start = -1;
    for (int i = 0; i < isAvailable.length; i++) {
      if (!isAvailable[i]) {
        if (start == -1) start = i;
      } else {
        if (start != -1) {
          // Gap ended at i-1
          intervals.add(model.newFixedInterval(start, i - start, prefix + "_off_" + start));
          start = -1;
        }
      }
    }
    if (start != -1) {
      intervals.add(
          model.newFixedInterval(start, isAvailable.length - start, prefix + "_off_" + start));
    }

    return intervals;
  }

  private Domain calculateAllowedStarts(
      SubjectResponse subject, int totalHours, int hpd, int duration) {
    List<Long> forcedGaps = new ArrayList<>(); // Indices that are NOT allowed

    // 1. Day Boundaries: A lesson cannot start at hour H if it spans across
    // midnight/day-boundary
    // i.e., defined by (h % hpd) + duration <= hpd
    for (int h = 0; h < totalHours; h++) {
      if ((h % hpd) + duration > hpd) {
        forcedGaps.add((long) h);
      }
    }

    // 2. Subject Unavailability (if any - implicit in request "For Subjects with
    // TimeOff")
    // NOTE: Subject object in code doesn't typically have "Availabilities" field
    // based on API,
    // but if it did, logic is same. Assuming currently no subject availability
    // field in provided context objects,
    // but if logic requires it, we'd check it here. Skipping for now as
    // SubjectResponse doesn't show it.

    Domain allowed = Domain.fromValues(IntStream.range(0, totalHours).mapToLong(i -> i).toArray());
    if (!forcedGaps.isEmpty()) {
      Domain forbidden = Domain.fromValues(forcedGaps.stream().mapToLong(l -> l).toArray());
      allowed =
          Domain.fromValues(IntStream.range(0, totalHours).mapToLong(i -> i).toArray())
              .intersectionWith(forbidden.complement()); // Effectively Allowed - Forbidden
    }

    return allowed;
  }

  private IntVar getOrCreateWeekVar(CpModel m, ModelVariables v, OrTLesson l, LessonFrequency f) {
    if (l.syncId() != null && v.getSyncIdToWeekVar().containsKey(l.syncId()))
      return v.getSyncIdToWeekVar().get(l.syncId());
    if (v.getLessonWeekVars().containsKey(l.id())) return v.getLessonWeekVars().get(l.id());

    IntVar w = m.newIntVar(0, 1, "week_l" + l.id()); // Only 0 or 1 for Bi-Weekly
    v.getLessonWeekVars().put(l.id(), w);
    if (l.syncId() != null) v.getSyncIdToWeekVar().put(l.syncId(), w);
    return w;
  }
}
