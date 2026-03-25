package com.sarmich.timetable.controller;

import com.sarmich.timetable.model.request.TeacherRequest;
import com.sarmich.timetable.model.response.TeacherResponse;
import com.sarmich.timetable.service.TeacherService;
import com.sarmich.timetable.model.request.TeacherUpdateRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("teacher")
@Log4j2
@AllArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    @PostMapping
    public TeacherResponse create(@Valid @RequestBody TeacherRequest dto) {
        return teacherService.create(dto);
    }

    @PutMapping
    public TeacherResponse editTeacher(@Valid @RequestBody TeacherUpdateRequest dto) {
        return teacherService.update(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteSubject(@PathVariable Long id) {
        teacherService.delete(id);
    }

    @GetMapping("/{id}")
    public TeacherResponse findById(@PathVariable Long id) {
        return teacherService.get(id);
    }

    @GetMapping
    public List<TeacherResponse> findAll() {
        return teacherService.findAll();
    }
}
