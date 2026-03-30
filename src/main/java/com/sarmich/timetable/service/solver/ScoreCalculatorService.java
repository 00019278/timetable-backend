package com.sarmich.timetable.service.solver;

import com.sarmich.timetable.domain.TimetableDataEntity;
import java.time.DayOfWeek;
import java.util.*;

import com.sarmich.timetable.model.TimetableGroupDetail;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ScoreCalculatorService {

    /**
     * Jadvalni baholash uchun asosiy metod.
     */
    public ScheduleScore calculateScore(
            List<TimetableDataEntity> schedule, ApplySoftConstraint rules) {

        // 1. Rejalashtirilmagan darslarni aniqlash
        int unscheduledCount =
                (int)
                        schedule.stream()
                                .filter(entity -> Boolean.FALSE.equals(entity.getIsScheduled()))
                                .count();

        // 2. Rejalashtirilgan darslarni ajratib olamiz
        List<TimetableDataEntity> activeLessons =
                schedule.stream().filter(entity -> Boolean.TRUE.equals(entity.getIsScheduled())).toList();

        // 3. Ma'lumotlarni Maplarga ajratish
        Map<Integer, Map<Integer, Map<DayOfWeek, Set<Integer>>>> teacherSchedules = new HashMap<>();
        Map<Integer, Map<Integer, Map<DayOfWeek, Set<Integer>>>> classSchedules = new HashMap<>();

        populateSchedules(activeLessons, teacherSchedules, classSchedules);

        // 4. Oynalarni (Gaps) hisoblash
        int teacherGaps = countTotalGaps(teacherSchedules);
        int classGaps = countTotalGaps(classSchedules);

        // 5. Umumiy Jarimani hisoblash
        int totalPenalty = 0;

        if (Boolean.TRUE.equals(rules.getApplyUnScheduledLessons())) {
            totalPenalty += unscheduledCount * rules.getApplyUnScheduledLessonsPenalty();
        }
        if (Boolean.TRUE.equals(rules.getApplyContinuityPenaltyTeacher())) {
            totalPenalty += teacherGaps * rules.getApplyContinuityPenaltyTeacherPenalty();
        }
        if (Boolean.TRUE.equals(rules.getApplyContinuityPenaltyClass())) {
            totalPenalty += classGaps * rules.getApplyContinuityPenaltyClassPenalty();
        }

        return new ScheduleScore(totalPenalty, teacherGaps, classGaps, unscheduledCount);
    }

    /**
     * Barcha darslarni aylanib chiqib, O'qituvchi va Sinf jadvallarini to'ldiradi.
     */
    private void populateSchedules(
            List<TimetableDataEntity> lessons,
            Map<Integer, Map<Integer, Map<DayOfWeek, Set<Integer>>>> teacherSchedules,
            Map<Integer, Map<Integer, Map<DayOfWeek, Set<Integer>>>> classSchedules) {

        for (TimetableDataEntity lesson : lessons) {
            DayOfWeek day = lesson.getDayOfWeek();
            Integer startHour = lesson.getHour();

            // Week Index ni aniqlash (0, 1 yoki ikkalasi)
            List<Integer> targetWeeks = new ArrayList<>();
            if (lesson.getWeekIndex() != null) {
                targetWeeks.add(lesson.getWeekIndex());
            } else {
                targetWeeks.add(0); // Week A
                targetWeeks.add(1); // Week B
            }

            // --- 1. SINF JADVALINI TO'LDIRISH ---
            // Sinf uchun duration bitta bo'ladi (chunki parallel darslar baribir bir xil vaqtda tugaydi).
            // Slot ichidagi birinchi detaildan durationni olsak yetarli.
            int classDuration = 1;
            if (lesson.getSlotDetails() != null && !lesson.getSlotDetails().isEmpty()) {
                classDuration = getDurationFromDetail(lesson.getSlotDetails().get(0));
            }

            if (lesson.getClassId() != null) {
                fillMap(classSchedules, lesson.getClassId(), targetWeeks, day, startHour, classDuration);
            }

            // --- 2. O'QITUVCHI JADVALINI TO'LDIRISH ---
            // O'qituvchilar uchun durationni HAR BIR DETAIL ichidan alohida olamiz.
            if (lesson.getSlotDetails() != null) {
                for (TimetableGroupDetail detail : lesson.getSlotDetails()) {
                    if (detail.getTeacherId() != null) {
                        Integer teacherId = detail.getTeacherId();
                        // Har bir o'qituvchi uchun o'z darsining davomiyligi
                        int teacherDuration = getDurationFromDetail(detail);

                        fillMap(teacherSchedules, teacherId, targetWeeks, day, startHour, teacherDuration);
                    }
                }
            }
        }
    }

    /**
     * TimetableGroupDetail ichidan originalLessonData ni olib, duration ni qaytaradi.
     */
    private int getDurationFromDetail(TimetableGroupDetail detail) {
        if (detail.getOriginalLessonData() != null) {
            Integer p = detail.getOriginalLessonData().period();
            if (p != null && p > 1) {
                return p;
            }
        }
        return 1; // Default duration
    }

    /**
     * Mapni to'ldirish uchun yordamchi metod.
     */
    private void fillMap(
            Map<Integer, Map<Integer, Map<DayOfWeek, Set<Integer>>>> scheduleMap,
            Integer entityId,
            List<Integer> weeks,
            DayOfWeek day,
            int startHour,
            int duration) {

        for (Integer week : weeks) {
            Map<DayOfWeek, Set<Integer>> daysMap =
                    scheduleMap
                            .computeIfAbsent(entityId, k -> new HashMap<>())
                            .computeIfAbsent(week, k -> new HashMap<>());

            Set<Integer> hoursSet = daysMap.computeIfAbsent(day, k -> new HashSet<>());

            // Periodni hisobga olib, soatlarni band qilamiz
            for (int i = 0; i < duration; i++) {
                hoursSet.add(startHour + i);
            }
        }
    }

    /**
     * Tayyorlangan Map asosida umumiy oynalarni sanash.
     */
    private int countTotalGaps(
            Map<Integer, Map<Integer, Map<DayOfWeek, Set<Integer>>>> groupedSchedule) {
        int totalGaps = 0;

        for (var weeksMap : groupedSchedule.values()) {
            for (var daysMap : weeksMap.values()) {
                for (Set<Integer> hours : daysMap.values()) {
                    List<Integer> sortedHours = new ArrayList<>(hours);
                    totalGaps += countGapsInDay(sortedHours);
                }
            }
        }
        return totalGaps;
    }

    /**
     * Bir kunlik soatlar ro'yxatidan oynalar sonini topadi.
     * Mantiq: (Max - Min + 1) - Count
     */
    private int countGapsInDay(List<Integer> hours) {
        if (hours == null || hours.size() <= 1) {
            return 0;
        }

        Collections.sort(hours);

        int minHour = hours.get(0);
        int maxHour = hours.get(hours.size() - 1);

        int span = maxHour - minHour + 1;
        int count = hours.size();

        return span - count;
    }
}