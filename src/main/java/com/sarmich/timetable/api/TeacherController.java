package com.sarmich.timetable.api;

import com.sarmich.timetable.model.UserPrincipal;
import com.sarmich.timetable.model.request.TeacherRequest;
import com.sarmich.timetable.model.request.TeacherUpdateRequest;
import com.sarmich.timetable.model.response.TeacherResponse;
import com.sarmich.timetable.service.TeacherService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("teacher")
@Log4j2
@AllArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    public TeacherResponse create(@AuthenticationPrincipal UserPrincipal userPrincipal, @Valid @RequestBody TeacherRequest dto) {
        return teacherService.create(userPrincipal.orgId(), dto);
    }

    @PutMapping
    public TeacherResponse editTeacher(@AuthenticationPrincipal UserPrincipal userPrincipal, @Valid @RequestBody TeacherUpdateRequest dto) {
        return teacherService.update(userPrincipal.orgId(), dto);
    }

    @DeleteMapping("/{id}")
    public void deleteSubject(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
        teacherService.delete(userPrincipal.orgId(), id);
    }

    @GetMapping("/{id}")
    public TeacherResponse findById(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
        return teacherService.get(userPrincipal.orgId(), id);
    }

    @GetMapping
    public List<TeacherResponse> findAll(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return teacherService.findAll(userPrincipal.orgId());
    }
}
