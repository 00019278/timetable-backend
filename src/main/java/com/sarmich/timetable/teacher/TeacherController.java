package com.sarmich.timetable.teacher;

import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("teacher")
@Log4j2
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

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
