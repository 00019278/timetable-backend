package com.sarmich.timetable.service.solver;

import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.sarmich.timetable.model.*;
import com.sarmich.timetable.model.response.OrTLesson;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Log4j2
public class SolutionProcessor {

    public SolverResult process(
            CpSolverStatus status, CpSolver solver, ModelVariables vars, ModelData data) {
        log.info("Solving finished with status: {}", status);

        List<TimetableSlotResponse> finalScheduledSlots = new ArrayList<>();
        List<UnscheduledLesson> unscheduledLessons = new ArrayList<>();

        // Mapni tayyorlash
        Map<Integer, OrTLesson> lessonMap =
                data.getLessons().stream().collect(Collectors.toMap(OrTLesson::id, l -> l));

        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            // 1. Yassi (Flat) ro'yxatni olamiz
            List<Response> flatResponses = extractScheduledLessons(solver, vars, data, lessonMap);

            // 2. Ularni guruhlab, chiroyli strukturaga o'tkazamiz
            finalScheduledSlots = groupResponses(flatResponses);

            // 3. Sig'magan darslarni aniqlaymiz
            unscheduledLessons = findUnscheduledLessons(flatResponses, data);
        } else {
            log.error("No solution found.");
            unscheduledLessons = markAllAsUnscheduled(data);
        }

        logSolverStatistics(solver);
        return new SolverResult(finalScheduledSlots, unscheduledLessons);
    }

    // --- TRANSFORMATSIYA METODI (YANGI) ---

    private List<TimetableSlotResponse> groupResponses(List<Response> flatResponses) {
        // Guruhlash kaliti: ClassID + Day + Hour + WeekIndex
        // Bizga shu parametrlari bir xil bo'lgan darslarni bitta "quti"ga solish kerak.

        Map<String, List<Response>> grouped = flatResponses.stream()
                .collect(Collectors.groupingBy(r ->
                        r.getClassObj().id() + "-" +
                                r.getDay() + "-" +
                                r.getHour() + "-" +
                                (r.getWeekIndex() == null ? "W" : r.getWeekIndex()) // Null safe key
                ));

        List<TimetableSlotResponse> result = new ArrayList<>();

        for (List<Response> groupItems : grouped.values()) {
            if (groupItems.isEmpty()) continue;

            // "Umumiy" ma'lumotlarni birinchi elementdan olamiz (chunki hammasida bir xil)
            Response first = groupItems.get(0);

            // "Ichki" (Details) ro'yxatni yasaymiz
            List<TimetableGroupDetail> details = groupItems.stream()
                    .map(item -> TimetableGroupDetail.builder()
                            .lessonId(item.getLessonId())
                            .subjectId(item.getSubject() != null ? item.getSubject().id() : null)
                            .teacherId(item.getTeacher() != null ? item.getTeacher().id() : null)
                            .roomId(item.getRoom() != null ? item.getRoom().id() : null)
                            .groupId(item.getGroup() != null ? item.getGroup().id() : null) // O'g'il bolalar, Qizlar yoki null
                            .build())
                    .collect(Collectors.toList());

            // Asosiy slotni yasaymiz
            TimetableSlotResponse slot = TimetableSlotResponse.builder()
                    .day(first.getDay())
                    .hour(first.getHour())
                    .period(first.getPeriod())
                    .weekIndex(first.getWeekIndex())
                    .classInfo(first.getClassObj())
                    .details(details) // <-- Detallar shu yerda
                    .build();

            result.add(slot);
        }

        // Saralash (Kun -> Soat -> Sinf)
        result.sort(Comparator.comparing(TimetableSlotResponse::getDay)
                .thenComparing(TimetableSlotResponse::getHour)
                .thenComparing(s -> s.getClassInfo().id()));

        return result;
    }

    // --- ESKI METODLAR (O'ZGARISHSIZ QOLADI, FAQAT Response return qiladi) ---

    private List<Response> extractScheduledLessons(
            CpSolver solver, ModelVariables vars, ModelData data, Map<Integer, OrTLesson> lessonMap) {

        return vars.getAssignmentVars().entrySet().stream()
                .filter(entry -> solver.booleanValue(entry.getValue()))
                .map(entry -> createResponseFromVarKey(entry.getKey(), data, solver, vars, lessonMap))
                .collect(Collectors.toList());
    }

    private Response createResponseFromVarKey(
            String varKey,
            ModelData data,
            CpSolver solver,
            ModelVariables vars,
            Map<Integer, OrTLesson> lessonMap) {

        String[] parts = varKey.split("_");
        // ... (indexlarni parse qilish qismi o'zgarishsiz) ...
        int cIdx = Integer.parseInt(parts[0].substring(1));
        int tIdx = Integer.parseInt(parts[1].substring(1));
        int sIdx = Integer.parseInt(parts[2].substring(1));
        int h = Integer.parseInt(parts[3].substring(1));

        Response response = new Response();
        response.setClassObj(data.getClassIndexToObj().get(cIdx));
        response.setTeacher(data.getTeacherIndexToObj().get(tIdx));
        response.setSubject(data.getSubjectIndexToObj().get(sIdx));
        response.setDay(DayOfWeek.of((h / data.getHoursPerDay()) + 1));
        response.setHour((h % data.getHoursPerDay()) + 1);

        int period = 1;
        int solverLessonId = 0; // Bu OrTLesson.id (1, 2, 3...)

        for (int i = 4; i < parts.length; i++) {
            String part = parts[i];
            if (part.startsWith("r")) {
                // ... room logic ...
                int rIndex = Integer.parseInt(part.substring(1));
                if (rIndex > 0 && data.getRoomIndexToObj().containsKey(rIndex)) {
                    response.setRoom(data.getRoomIndexToObj().get(rIndex));
                }
            } else if (part.startsWith("p")) {
                period = Integer.parseInt(part.substring(1));
            } else if (part.startsWith("l")) {
                solverLessonId = Integer.parseInt(part.substring(1));
            }
        }

        response.setPeriod(period);

        // --- MUHIM O'ZGARISH ---
        // Mapdan SOLVER ID orqali darsni topamiz
        OrTLesson originalLesson = lessonMap.get(solverLessonId);

        if (originalLesson != null) {
            // 1. Responsega ORIGINAL ID ni yozamiz (Bazaga shu kerak)
            response.setLessonId(originalLesson.originalId());

            // 2. Guruhni set qilamiz
            response.setGroup(originalLesson.group());
        } else {
            // Fallback (bu holat bo'lmasligi kerak)
            response.setLessonId(solverLessonId);
        }

        // ... (Week index qismi o'zgarishsiz) ...
        IntVar weekVar = vars.getLessonWeekVars().get(varKey);
        if (weekVar != null) {
            long weekValue = solver.value(weekVar);
            response.setWeekIndex((int) weekValue);
        } else {
            response.setWeekIndex(null);
        }

        return response;
    }

    // ... (findUnscheduledLessons, markAllAsUnscheduled, logSolverStatistics o'zgarishsiz) ...
    // Faqat findUnscheduledLessons da List<Response> ishlatiladi, bu to'g'ri.

    private List<UnscheduledLesson> findUnscheduledLessons(List<Response> scheduled, ModelData data) {
        List<UnscheduledLesson> unscheduled = new ArrayList<>();
        Map<Integer, List<Response>> scheduledByLessonId =
                scheduled.stream().collect(Collectors.groupingBy(Response::getLessonId));

        for (OrTLesson req : data.getLessons()) {
            List<Response> placedSlots = scheduledByLessonId.getOrDefault(req.id(), new ArrayList<>());
            int actualHoursCount =
                    placedSlots.stream().mapToInt(s -> s.getPeriod() != null ? s.getPeriod() : 1).sum();

            if (actualHoursCount < req.lessonCount()) {
                unscheduled.add(
                        new UnscheduledLesson(
                                req.classInfo() != null ? req.classInfo().id() : null,
                                req.teacher() != null ? req.teacher().id() : null,
                                req.subject() != null ? req.subject().id() : null,
                                req.rooms() != null ? req.rooms().stream().map(com.sarmich.timetable.model.response.RoomResponse::id).toList() : null,
                                req.lessonCount(),
                                actualHoursCount,
                                req.lessonCount() - actualHoursCount));
            }
        }
        if (!unscheduled.isEmpty()) {
            log.warn("Found {} unscheduled.", unscheduled.size());
        }
        return unscheduled;
    }

    // ... markAllAsUnscheduled va logSolverStatistics ...
    private List<UnscheduledLesson> markAllAsUnscheduled(ModelData data) {
        return data.getLessons().stream().map(req -> new UnscheduledLesson(
                req.classInfo() != null ? req.classInfo().id() : null,
                req.teacher() != null ? req.teacher().id() : null,
                req.subject() != null ? req.subject().id() : null,
                req.rooms() != null ? req.rooms().stream().map(com.sarmich.timetable.model.response.RoomResponse::id).toList() : null,
                req.lessonCount(), 0, req.lessonCount()
        )).collect(Collectors.toList());
    }

    private void logSolverStatistics(CpSolver solver) {
        log.info("Stats: conflicts={}, wall_time={}", solver.numConflicts(), solver.wallTime());
    }
}