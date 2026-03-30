package com.sarmich.timetable.model;

import java.time.DayOfWeek;
import java.util.List;

import com.sarmich.timetable.model.response.ClassResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimetableSlotResponse {
    private DayOfWeek day;
    private Integer hour;
    private Integer period;
    private Integer weekIndex; // 0 (A), 1 (B) yoki null (Weekly)
    private ClassResponse classInfo;
    
    // Shu vaqtda bo'ladigan darslar ro'yxati (Masalan: O'g'il bolalar + Qizlar)
    private List<TimetableGroupDetail> details; 
}