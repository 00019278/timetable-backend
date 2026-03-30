package com.sarmich.timetable.model;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public record TimetableDataResponse(
        UUID id,
        UUID timetableId,
        Boolean isScheduled,

        // Faqat ClassID qoladi (chunki qator Sinfga tegishli)
        Integer classId,

        DayOfWeek dayOfWeek,
        Integer hour,

        // Yangi: Hafta indeksi (Bi-weekly uchun)
        Integer weekIndex,

        // --- ENG MUHIM O'ZGARISH ---
        // Eski "scheduledData" o'rniga endi guruhlar ro'yxati qaytadi.
        // Frontend shu ro'yxatga qarab katakni bo'lib chizadi.
        List<TimetableGroupDetail> slotDetails,

        // Sig'magan darslar uchun
        Object unscheduledData, // Yoki UnscheduledLesson tipi

        Integer version
) {}