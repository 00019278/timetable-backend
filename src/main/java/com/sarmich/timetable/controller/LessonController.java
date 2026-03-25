package com.sarmich.timetable.controller;

import com.sarmich.timetable.model.request.LessonRequest;
import com.sarmich.timetable.model.response.LessonResponse;
import com.sarmich.timetable.service.LessonService;
import com.sarmich.timetable.model.request.LessonUpdateRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("lesson")
@AllArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping
    public LessonResponse addSubject(@Valid @RequestBody LessonRequest dto) {
        return lessonService.add(dto);
    }

    @PutMapping
    public LessonResponse editSubject(@Valid @RequestBody LessonUpdateRequest dto) {
        return lessonService.update(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteSubject(@PathVariable Long id) {
        lessonService.delete(id);
    }

    @GetMapping("/{id}")
    public LessonResponse getSubject(@PathVariable Long id) {
        return lessonService.get(id);
    }

    @GetMapping
    public PageImpl<LessonResponse> getList(Pageable pageable) {
        return lessonService.getAll(pageable);
    }
}
