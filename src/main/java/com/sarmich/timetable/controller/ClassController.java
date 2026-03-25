package com.sarmich.timetable.controller;

import com.sarmich.timetable.model.request.ClassRequest;
import com.sarmich.timetable.model.response.ClassResponse;
import com.sarmich.timetable.service.ClassService;
import com.sarmich.timetable.model.request.ClassUpdateRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("class")
@AllArgsConstructor
public class ClassController {

    private final ClassService classService;

    @PostMapping
    public ClassResponse addSubject(@Valid @RequestBody ClassRequest dto) {
        return classService.add(dto);
    }

    @PutMapping
    public ClassResponse editSubject(@Valid @RequestBody ClassUpdateRequest dto) {
        return classService.update(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteSubject(@PathVariable Long id) {
        classService.delete(id);
    }

    @GetMapping("/{id}")
    public ClassResponse getSubject(@PathVariable Long id) {
        return classService.get(id);
    }

    @GetMapping
    public PageImpl<ClassResponse> getList(Pageable pageable) {
        return classService.getAll(pageable);
    }
}
