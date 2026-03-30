package com.sarmich.timetable.api;

import com.sarmich.timetable.model.UserPrincipal;
import com.sarmich.timetable.model.common.Response;
import com.sarmich.timetable.model.request.BuildingRequest;
import com.sarmich.timetable.model.request.BuildingUpdateRequest;
import com.sarmich.timetable.model.response.BuildingResponse;
import com.sarmich.timetable.service.BuildingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/buildings")
@RequiredArgsConstructor
public class BuildingController {

  private final BuildingService buildingService;

  @PostMapping
  public Response<BuildingResponse> add(
      @PathVariable Integer orgId, @RequestBody BuildingRequest request) {
    return Response.ok(buildingService.add(orgId, request));
  }

  @PutMapping("/{id}")
  public Response<BuildingResponse> update(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Integer id,
      @RequestBody BuildingUpdateRequest request) {
    return Response.ok(buildingService.update(userPrincipal.orgId(), id, request));
  }

  @DeleteMapping("/{id}")
  public Response<Void> delete(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
    buildingService.delete(userPrincipal.orgId(), id);
    return Response.ok();
  }

  @GetMapping
  public Response<Page<BuildingResponse>> getAll(
      @AuthenticationPrincipal UserPrincipal userPrincipal, Pageable pageable) {
    return Response.ok(buildingService.getAll(userPrincipal.orgId(), pageable));
  }

  @GetMapping("/{id}")
  public Response<BuildingResponse> getById(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
    return Response.ok(buildingService.getById(userPrincipal.orgId(), id));
  }

  @GetMapping("/all")
  public Response<List<BuildingResponse>> getById(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return Response.ok(buildingService.findAll(userPrincipal.orgId()));
  }
}
