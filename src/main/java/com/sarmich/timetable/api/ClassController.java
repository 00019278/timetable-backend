package com.sarmich.timetable.api;

import com.sarmich.timetable.model.UserPrincipal;
import com.sarmich.timetable.model.request.ClassRequest;
import com.sarmich.timetable.model.response.ClassResponse;
import com.sarmich.timetable.service.ClassService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("class")
@AllArgsConstructor
public class ClassController {

    private final ClassService classService;

    @PostMapping
    public ClassResponse addSubject(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody ClassRequest dto) {
        return classService.add(userPrincipal.orgId(), dto);
    }

    @PutMapping("/{id}")
    public ClassResponse editSubject(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id, @RequestBody ClassRequest dto) {
        return classService.update(userPrincipal.orgId(), id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteSubject(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
        classService.delete(userPrincipal.orgId(), id);
    }

    @GetMapping("/{id}")
    public ClassResponse getSubject(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
        return classService.get(userPrincipal.orgId(), id);
    }

    @GetMapping
    public PageImpl<ClassResponse> getList(@AuthenticationPrincipal UserPrincipal userPrincipal, Pageable pageable) {
        return classService.getAll(userPrincipal.orgId(), pageable);
    }
}
