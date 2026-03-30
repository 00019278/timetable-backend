package com.sarmich.timetable.api;

import com.sarmich.timetable.model.UserPrincipal;
import com.sarmich.timetable.model.request.SubjectRequest;
import com.sarmich.timetable.model.request.SubjectUpdateRequest;
import com.sarmich.timetable.model.response.SubjectResponse;
import com.sarmich.timetable.service.SubjectService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("subject")
@AllArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping
    public SubjectResponse addSubject(@AuthenticationPrincipal UserPrincipal userPrincipal, @Valid @RequestBody SubjectRequest dto) {
        return subjectService.add(userPrincipal.orgId(), dto);
    }

    @PutMapping("/{id}")
    public SubjectResponse editSubject(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id, @RequestBody SubjectRequest dto) {
        return subjectService.update(userPrincipal.orgId(), id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteSubject(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
        subjectService.delete(userPrincipal.orgId(), id);
    }

    @GetMapping("/{id}")
    public SubjectResponse getSubject(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
        return subjectService.get(userPrincipal.orgId(), id);
    }

    @GetMapping
    public PageImpl<SubjectResponse> getList(@AuthenticationPrincipal UserPrincipal userPrincipal, Pageable pageable) {
        return subjectService.getAll(userPrincipal.orgId(), pageable);
    }
}
