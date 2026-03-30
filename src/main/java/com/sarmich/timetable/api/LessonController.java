package com.sarmich.timetable.api;

import com.sarmich.timetable.model.UserPrincipal;
import com.sarmich.timetable.model.common.Response;
import com.sarmich.timetable.model.request.LessonRequest;
import com.sarmich.timetable.model.response.LessonResponse;
import com.sarmich.timetable.model.response.LessonsWithMetadataResponse;
import com.sarmich.timetable.service.ImportTaqsimotService;
import com.sarmich.timetable.service.LessonService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/lessons/v1")
@AllArgsConstructor
public class LessonController {

  private final LessonService lessonService;
  private final ImportTaqsimotService importTaqsimotService;

  @PostMapping
  public Response<Void> create(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody LessonRequest dto) {
    lessonService.add(userPrincipal.orgId(), dto);
    return Response.ok();
  }

  @PutMapping("/{id}")
  public Response<Void> edit(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Integer id,
      @RequestBody LessonRequest dto) {
    lessonService.update(userPrincipal.orgId(), id, dto);
    return Response.ok();
  }

  @DeleteMapping("/{id}")
  public Response<Void> delete(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
    lessonService.delete(userPrincipal.orgId(), id);
    return Response.ok();
  }

  @GetMapping("/{id}")
  public Response<LessonResponse> find(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
    return Response.ok(lessonService.get(userPrincipal.orgId(), id));
  }

  @GetMapping
  public Response<PageImpl<LessonResponse>> findAll(
      @AuthenticationPrincipal UserPrincipal userPrincipal, Pageable pageable) {
    return Response.ok(lessonService.getAll(userPrincipal.orgId(), pageable));
  }

  @GetMapping("/all")
  public Response<LessonsWithMetadataResponse> findAllWithMetadata(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return Response.ok(lessonService.getAllLessonsWithMetadata(userPrincipal.orgId()));
  }

  @PostMapping("/template")
  public Response<Void> importFromTemplate(
      @AuthenticationPrincipal UserPrincipal userPrincipal, MultipartFile file) throws Exception {
    importTaqsimotService.importExcelToDb(file, userPrincipal.orgId());
    return Response.ok();
  }
}
