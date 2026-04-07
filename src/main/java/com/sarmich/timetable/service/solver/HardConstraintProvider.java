package com.sarmich.timetable.service.solver;

import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.sarmich.timetable.model.response.OrTLesson;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class HardConstraintProvider {

  public void apply(CpModel model, ModelVariables vars, ModelData data) {
    log.info("Applying all hard constraints to the model (Interval-based)...");

    // 1. Resource Exclusivity (NoOverlap)
    addResourceExclusivityConstraint(model, vars, data);

    // 2. Room Capacity (NoOverlap for now, assuming 1 room = 1 slot)
    addRoomCapacityConstraints(model, vars, data);

    // 3. Simultaneous Lessons
    addSimultaneousLessonsConstraint(model, vars, data);

    // Note: Weekly/Daily subject limits and distribution are implicitly handled if
    // we schedule exactly what is in the list.
    // If we need to enforce "Max 2 per day" for a set of lessons, that requires
    // extra constraints on 'start' vars.
    // For 'Performance comparable to ASC', minimizing vars is key. Detailed daily
    // limits might be Soft or require custom constraints.
    // We skip complex Daily Limits for now to ensure foundational Interval model
    // works.
  }

  private void addResourceExclusivityConstraint(
      CpModel model, ModelVariables vars, ModelData data) {
    log.debug("Applying NoOverlap constraints for Teachers and Classes.");

    // Teachers
    for (Map.Entry<Integer, List<IntervalVar>> entry : vars.getTeacherIntervalsA().entrySet()) {
      if (!entry.getValue().isEmpty()) {
        model.addNoOverlap(entry.getValue().toArray(new IntervalVar[0]));
      }
    }
    for (Map.Entry<Integer, List<IntervalVar>> entry : vars.getTeacherIntervalsB().entrySet()) {
      if (!entry.getValue().isEmpty()) {
        model.addNoOverlap(entry.getValue().toArray(new IntervalVar[0]));
      }
    }

    // Classes (Whole Class Lessons & Global Class TimeOffs)
    for (Map.Entry<Integer, List<IntervalVar>> entry : vars.getClassIntervalsA().entrySet()) {
      if (!entry.getValue().isEmpty()) {
        model.addNoOverlap(entry.getValue().toArray(new IntervalVar[0]));
      }
    }
    for (Map.Entry<Integer, List<IntervalVar>> entry : vars.getClassIntervalsB().entrySet()) {
      if (!entry.getValue().isEmpty()) {
        model.addNoOverlap(entry.getValue().toArray(new IntervalVar[0]));
      }
    }

    // === GROUP NOOVERLAP ===
    // Har bir guruhning o'z intervallari o'zaro kesishmasligi kerak!
    // Bu juda muhim - aks holda bir guruh uchun bir nechta dars bir vaqtda
    // joylashadi.
    for (Map.Entry<Integer, List<IntervalVar>> entry : vars.getGroupIntervalsA().entrySet()) {
      if (entry.getValue().size() > 1) {
        model.addNoOverlap(entry.getValue().toArray(new IntervalVar[0]));
      }
    }
    for (Map.Entry<Integer, List<IntervalVar>> entry : vars.getGroupIntervalsB().entrySet()) {
      if (entry.getValue().size() > 1) {
        model.addNoOverlap(entry.getValue().toArray(new IntervalVar[0]));
      }
    }

    // === GROUP + CLASS COMBINED NOOVERLAP ===
    // Guruh darslari va butun sinf darslari bir vaqtda bo'lmasligi kerak.
    // Groups: Group Lesson overlap with Whole Class Lesson
    // For each Group, collect its intervals AND its parent Class intervals.
    // We need to know which class a group belongs to.
    // Option: Iterate all groups in ModelData or build map.
    // Using ModelData uniqueClasses to find groups.

    for (com.sarmich.timetable.model.response.ClassResponse c : data.getUniqueClasses()) {
      int cIdx = data.getClassIdToIndex().get(c.id());
      List<IntervalVar> classIntervalsA =
          vars.getClassIntervalsA().getOrDefault(cIdx, Collections.emptyList());
      List<IntervalVar> classIntervalsB =
          vars.getClassIntervalsB().getOrDefault(cIdx, Collections.emptyList());

      if (c.groups() != null) {
        for (com.sarmich.timetable.model.response.GroupResponse g : c.groups()) {
          // Week A
          List<IntervalVar> groupIntervalsA =
              vars.getGroupIntervalsA().getOrDefault(g.id(), Collections.emptyList());
          // Faqat CLASS intervallari bilan combined qilamiz (Group o'z ichida yuqorida
          // qilindi)
          if (!classIntervalsA.isEmpty() && !groupIntervalsA.isEmpty()) {
            java.util.ArrayList<IntervalVar> combined = new java.util.ArrayList<>();
            combined.addAll(classIntervalsA);
            combined.addAll(groupIntervalsA);
            model.addNoOverlap(combined.toArray(new IntervalVar[0]));
          }

          // Week B
          List<IntervalVar> groupIntervalsB =
              vars.getGroupIntervalsB().getOrDefault(g.id(), Collections.emptyList());
          if (!classIntervalsB.isEmpty() && !groupIntervalsB.isEmpty()) {
            java.util.ArrayList<IntervalVar> combined = new java.util.ArrayList<>();
            combined.addAll(classIntervalsB);
            combined.addAll(groupIntervalsB);
            model.addNoOverlap(combined.toArray(new IntervalVar[0]));
          }
        }
      }
    }
  }

  private void addRoomCapacityConstraints(CpModel model, ModelVariables vars, ModelData data) {
    if (!data.isUseRooms()) return;
    log.debug("Applying NoOverlap constraints for Rooms.");

    // Rooms (Week A)
    for (Map.Entry<Integer, List<IntervalVar>> entry : vars.getRoomIntervalsA().entrySet()) {
      if (!entry.getValue().isEmpty()) {
        model.addNoOverlap(entry.getValue().toArray(new IntervalVar[0]));
      }
    }
    // Rooms (Week B)
    for (Map.Entry<Integer, List<IntervalVar>> entry : vars.getRoomIntervalsB().entrySet()) {
      if (!entry.getValue().isEmpty()) {
        model.addNoOverlap(entry.getValue().toArray(new IntervalVar[0]));
      }
    }
  }

  private void addSimultaneousLessonsConstraint(
      CpModel model, ModelVariables vars, ModelData data) {
    log.debug("Applying simultaneous (sync) lessons constraints.");

    Map<String, List<OrTLesson>> syncGroups =
        data.getLessons().stream()
            .filter(l -> l.syncId() != null)
            .collect(Collectors.groupingBy(OrTLesson::syncId));

    for (List<OrTLesson> group : syncGroups.values()) {
      if (group.size() < 2) continue;

      OrTLesson leader = group.get(0);
      IntVar leaderStart = vars.getLessonStartVars().get(leader.id());
      // IntVar leaderWeek = vars.getLessonWeekVars().get(leader.id()); // If
      // bi-weekly

      for (int i = 1; i < group.size(); i++) {
        OrTLesson follower = group.get(i);
        IntVar followerStart = vars.getLessonStartVars().get(follower.id());

        if (leaderStart != null && followerStart != null) {
          model.addEquality(leaderStart, followerStart);
        }

        // If Bi-Weekly, we generally want them to be on the same week cycle too?
        // Or maybe they are swapped?
        // Usually "Sync" means "Same Time".
        // We should also sync the weekVar if it exists.
        if (vars.getLessonWeekVars().containsKey(leader.id())
            && vars.getLessonWeekVars().containsKey(follower.id())) {
          model.addEquality(
              vars.getLessonWeekVars().get(leader.id()),
              vars.getLessonWeekVars().get(follower.id()));
        }
      }
    }
  }
}
