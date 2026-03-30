package com.sarmich.timetable.api;

import com.sarmich.timetable.model.UserPrincipal;
import com.sarmich.timetable.model.common.Response;
import com.sarmich.timetable.model.request.ApplyToOthersRequest;
import com.sarmich.timetable.model.request.ClassRequest;
import com.sarmich.timetable.model.request.ClassUpdateRequest;
import com.sarmich.timetable.model.response.ClassResponse;
import com.sarmich.timetable.service.ClassService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes/v1")
@AllArgsConstructor
public class ClassController {

  private final ClassService classService;

  @PostMapping
  public Response<ClassResponse> addSubject(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody ClassRequest dto) {
    classService.add(userPrincipal.orgId(), dto);
    return Response.ok();
  }

  @PutMapping("/{id}")
  public Response<Void> editSubject(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Integer id,
      @RequestBody ClassUpdateRequest dto) {
    classService.update(userPrincipal.orgId(), id, dto);
    return Response.ok();
  }

  @DeleteMapping("/{id}")
  public Response<Void> deleteSubject(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
    classService.delete(userPrincipal.orgId(), id);
    return Response.ok();
  }

  @GetMapping("/{id}")
  public Response<ClassResponse> findById(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
    return Response.ok(classService.findById(userPrincipal.orgId(), id));
  }

  @GetMapping
  public Response<PageImpl<ClassResponse>> getList(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(required = false) Boolean withRooms,
      @RequestParam(required = false) Boolean withTeacher,
      Pageable pageable) {
    return Response.ok(
        classService.getAll(userPrincipal.orgId(), withRooms, withTeacher, pageable));
  }

  @GetMapping("/all")
  public Response<List<ClassResponse>> getList(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(required = false) Boolean withRooms,
      @RequestParam(required = false) Boolean withTeacher) {
    return Response.ok(classService.getAllClass(userPrincipal.orgId(), withRooms, withTeacher));
  }

  @PostMapping("/timeoff")
  public Response<Void> applyToOthers(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody ApplyToOthersRequest dto) {
    classService.applyToOthers(userPrincipal.orgId(), dto);
    return Response.ok();
  }
}
