package com.sarmich.timetable.service.solver;

import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.sarmich.timetable.model.*;
import com.sarmich.timetable.model.response.OrTLesson;
import com.sarmich.timetable.model.response.RoomResponse;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class SolutionProcessor {

  public SolverResult process(
      CpSolverStatus status, CpSolver solver, ModelVariables vars, ModelData data) {
    log.info("Processing solver results. Status: {}", status);

    if (status != CpSolverStatus.OPTIMAL && status != CpSolverStatus.FEASIBLE) {
      log.warn("Solver did not find a solution. Status: {}", status);
      return new SolverResult(new ArrayList<>(), markAllAsUnscheduled(data));
    }

    Map<Integer, OrTLesson> lessonMap =
        data.getLessons().stream().collect(Collectors.toMap(OrTLesson::id, l -> l));

    // 1. Extract scheduled lessons
    List<Response> flatResponses = new ArrayList<>();
    for (Map.Entry<Integer, IntVar> entry : vars.getLessonStartVars().entrySet()) {
      Integer solverId = entry.getKey();
      IntVar startVar = entry.getValue();
      OrTLesson lesson = lessonMap.get(solverId);

      if (lesson == null) continue;

      long startVal = solver.value(startVar);
      int hpd = data.getHoursPerDay();
      int dayIndex = (int) (startVal / hpd);
      int hourIndex = (int) (startVal % hpd);

      // Hafta aniqlash
      Integer weekIndex = null;
      if (vars.getLessonWeekVars().containsKey(solverId)) {
        weekIndex = (int) solver.value(vars.getLessonWeekVars().get(solverId));
      }

      Response res = new Response();
      res.setSolverLessonId(solverId);
      res.setLessonId(lesson.originalId());
      res.setClassObj(lesson.classInfo());
      res.setTeacher(lesson.teacher());
      res.setSubject(lesson.subject());
      res.setGroup(lesson.group());
      res.setDay(DayOfWeek.of(dayIndex + 1));
      res.setHour(hourIndex + 1);
      res.setPeriod(lesson.period() != null ? lesson.period() : 1);
      res.setWeekIndex(weekIndex);
      flatResponses.add(res);
    }

    // 2. Post-process: Assign Rooms
    assignRoomsToResponses(flatResponses, data, lessonMap);

    // 3. Grouping (Sinf + Kun + Soat + Hafta bo'yicha)
    List<TimetableSlotResponse> scheduledSlots = groupResponses(flatResponses);

    // 4. Calculate Unscheduled (Interval modelda status OPTIMAL bo'lsa, hammasi joylashgan bo'ladi)
    List<UnscheduledLesson> unscheduled = new ArrayList<>();

    log.info("Scheduled {} lessons into {} slots", flatResponses.size(), scheduledSlots.size());
    return new SolverResult(scheduledSlots, unscheduled);
  }

  private List<TimetableSlotResponse> groupResponses(List<Response> flatResponses) {
    // Grouping key: ClassId + Day + Hour + WeekIndex
    Map<String, List<Response>> grouped =
        flatResponses.stream()
            .collect(
                Collectors.groupingBy(
                    r ->
                        r.getClassObj().id()
                            + "-"
                            + r.getDay().getValue()
                            + "-"
                            + r.getHour()
                            + "-"
                            + (r.getWeekIndex() == null ? "W" : r.getWeekIndex())));

    List<TimetableSlotResponse> result = new ArrayList<>();
    for (List<Response> items : grouped.values()) {
      Response first = items.get(0);
      List<TimetableGroupDetail> details =
          items.stream()
              .map(
                  item ->
                      TimetableGroupDetail.builder()
                          .lessonId(item.getLessonId())
                          .subjectId(item.getSubject().id())
                          .teacherId(item.getTeacher() != null ? item.getTeacher().id() : null)
                          .roomId(item.getRoom() != null ? item.getRoom().id() : null)
                          .groupId(item.getGroup() != null ? item.getGroup().id() : null)
                          .build())
              .collect(Collectors.toList());

      result.add(
          TimetableSlotResponse.builder()
              .day(first.getDay())
              .hour(first.getHour())
              .period(first.getPeriod())
              .weekIndex(first.getWeekIndex())
              .classInfo(first.getClassObj())
              .details(details)
              .build());
    }
    return result;
  }

  private void assignRoomsToResponses(
      List<Response> responses, ModelData data, Map<Integer, OrTLesson> lessonMap) {
    // Oddiy greedy assignment (hali solver ichiga kirmagan bo'lsa)
    Map<String, Set<Integer>> occupied = new HashMap<>();
    for (Response r : responses) {
      OrTLesson lesson = lessonMap.get(r.getSolverLessonId());
      if (lesson.rooms() == null || lesson.rooms().isEmpty()) continue;

      for (RoomResponse room : lesson.rooms()) {
        String key =
            r.getDay()
                + "-"
                + r.getHour()
                + "-"
                + (r.getWeekIndex() == null ? "W" : r.getWeekIndex());
        if (!occupied.computeIfAbsent(key, k -> new HashSet<>()).contains(room.id())) {
          r.setRoom(room);
          occupied.get(key).add(room.id());
          break;
        }
      }
    }
  }

  private List<UnscheduledLesson> markAllAsUnscheduled(ModelData data) {
    return data.getLessons().stream()
        .map(
            l ->
                new UnscheduledLesson(
                    l.classInfo().id(),
                    l.teacher() != null ? l.teacher().id() : null,
                    l.subject().id(),
                    null,
                    l.lessonCount(),
                    0,
                    l.lessonCount()))
        .toList();
  }
}
