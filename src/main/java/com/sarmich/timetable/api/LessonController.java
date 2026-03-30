package com.sarmich.timetable.api;

import com.sarmich.timetable.model.UserPrincipal;
import com.sarmich.timetable.model.request.LessonRequest;
import com.sarmich.timetable.model.request.LessonUpdateRequest;
import com.sarmich.timetable.model.response.LessonResponse;
import com.sarmich.timetable.service.LessonService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("lesson")
@AllArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping
    public LessonResponse addSubject(@AuthenticationPrincipal UserPrincipal userPrincipal, @Valid @RequestBody LessonRequest dto) {
        return lessonService.add(userPrincipal.orgId(), dto);
    }

    @PutMapping
    public LessonResponse editSubject(@AuthenticationPrincipal UserPrincipal userPrincipal, @Valid @RequestBody LessonUpdateRequest dto) {
        return lessonService.update(userPrincipal.orgId(), dto);
    }

    @DeleteMapping("/{id}")
    public void deleteSubject(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
        lessonService.delete(userPrincipal.orgId(), id);
    }

    @GetMapping("/{id}")
    public LessonResponse getSubject(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
        return lessonService.get(userPrincipal.orgId(), id);
    }

    @GetMapping
    public PageImpl<LessonResponse> getList(@AuthenticationPrincipal UserPrincipal userPrincipal, Pageable pageable) {
        return lessonService.getAll(userPrincipal.orgId(), pageable);
    }
}
