package com.sarmich.timetable.subject;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("subject")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping
    public SubjectResponse addSubject(@Valid @RequestBody SubjectRequest dto) {
        return subjectService.add(dto);
    }

    @PutMapping
    public SubjectResponse editSubject(@Valid @RequestBody SubjectUpdateDto dto) {
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
