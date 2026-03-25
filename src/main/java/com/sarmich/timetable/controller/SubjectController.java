package com.sarmich.timetable.controller;

import com.sarmich.timetable.model.request.SubjectRequest;
import com.sarmich.timetable.model.response.SubjectResponse;
import com.sarmich.timetable.service.SubjectService;
import com.sarmich.timetable.model.request.SubjectUpdateRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("subject")
@AllArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public SubjectResponse addSubject(@Valid @RequestBody SubjectRequest dto) {
        return subjectService.add(dto);
    }

    @PutMapping
    public SubjectResponse editSubject(@Valid @RequestBody SubjectUpdateRequest dto) {
        return subjectService.update(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteSubject(@PathVariable Long id) {
        subjectService.delete(id);
    }

    @GetMapping("/{id}")
    public SubjectResponse getSubject(@PathVariable Long id) {
        return subjectService.get(id);
    }

    @GetMapping
    public PageImpl<SubjectResponse> getList(Pageable pageable) {
        return subjectService.getAll(pageable);
    }
}
