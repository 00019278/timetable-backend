package com.sarmich.timetable.lesson;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LessonRequest(

        Long subjectId,
        Long teacherId,
        Long classId,
        Integer lessonCount

) {
}