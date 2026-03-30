package com.sarmich.timetable.api;

import com.sarmich.timetable.model.UserPrincipal;
import com.sarmich.timetable.model.common.Response;
import com.sarmich.timetable.model.request.TeacherRequest;
import com.sarmich.timetable.model.request.TeacherUpdateRequest;
import com.sarmich.timetable.model.response.TeacherResponse;
import com.sarmich.timetable.service.TeacherService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers/v1")
@AllArgsConstructor
public class TeacherController {

  private final TeacherService teacherService;

  @PostMapping
  public Response<Void> create(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Valid @RequestBody TeacherRequest dto) {
    teacherService.create(userPrincipal.orgId(), dto);
    return Response.ok();
  }

  @PutMapping("/{id}")
  public Response<TeacherResponse> editTeacher(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Integer id,
      @Valid @RequestBody TeacherUpdateRequest dto) {
    teacherService.update(userPrincipal.orgId(), id, dto);
    return Response.ok();
  }

  @DeleteMapping("/{id}")
  public Response<Void> deleteSubject(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
    teacherService.delete(userPrincipal.orgId(), id);
    return Response.ok();
  }

  @GetMapping("/{id}")
  public Response<TeacherResponse> findById(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
    return Response.ok(teacherService.get(userPrincipal.orgId(), id));
  }

  @GetMapping("/all")
  public Response<List<TeacherResponse>> findAll(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(required = false) Boolean withSubjects) {
    return Response.ok(teacherService.findAll(userPrincipal.orgId(), withSubjects));
  }

  @GetMapping
  public Response<Page<TeacherResponse>> findAll(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(required = false) Boolean withSubjects,
      Pageable pageable) {
    return Response.ok(teacherService.findAll(userPrincipal.orgId(), withSubjects, pageable));
  }
}
