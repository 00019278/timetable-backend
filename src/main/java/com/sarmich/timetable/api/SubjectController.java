package com.sarmich.timetable.api;

import com.sarmich.timetable.model.UserPrincipal;
import com.sarmich.timetable.model.common.Response;
import com.sarmich.timetable.model.request.SubjectRequest;
import com.sarmich.timetable.model.response.SubjectResponse;
import com.sarmich.timetable.service.SubjectService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects/v1")
@AllArgsConstructor
public class SubjectController {

  private final SubjectService subjectService;

  @PostMapping
  public Response<SubjectResponse> addSubject(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Valid @RequestBody SubjectRequest dto) {
    return Response.ok(subjectService.add(userPrincipal.orgId(), dto));
  }

  @PutMapping("/{id}")
  public Response<SubjectResponse> editSubject(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Integer id,
      @RequestBody SubjectRequest dto) {
    return Response.ok(subjectService.update(userPrincipal.orgId(), id, dto));
  }

  @DeleteMapping("/{id}")
  public Response<Void> deleteSubject(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
    subjectService.delete(userPrincipal.orgId(), id);
    return Response.ok();
  }

  @GetMapping("/{id}")
  public Response<SubjectResponse> getSubject(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
    return Response.ok(subjectService.get(userPrincipal.orgId(), id));
  }

  @GetMapping
  public Response<Page<SubjectResponse>> getList(
      @AuthenticationPrincipal UserPrincipal userPrincipal, Pageable pageable) {
    return Response.ok(subjectService.getAll(userPrincipal.orgId(), pageable));
  }

  @GetMapping("/all")
  public Response<List<SubjectResponse>> getList(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return Response.ok(subjectService.getAllSub(userPrincipal.orgId()));
  }
}
