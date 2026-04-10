package com.sarmich.timetable.api;

import com.sarmich.timetable.model.UserPrincipal;
import com.sarmich.timetable.model.common.Response;
import com.sarmich.timetable.model.request.RoomRequest;
import com.sarmich.timetable.model.response.RoomResponse;
import com.sarmich.timetable.service.RoomService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
// ВОТ НАША МАГИЯ: Заставляем контроллер слушать оба варианта адреса
@RequestMapping({"/api/rooms/v1", "/api/v1/rooms"})
@AllArgsConstructor
public class RoomController {
  private final RoomService roomService;

  @PostMapping
  public Response<RoomResponse> addRoom(
          @AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody RoomRequest request) {
    return Response.ok(roomService.add(userPrincipal.orgId(), request));
  }

// ... (остальной код оставь без изменений)

  @PutMapping("/{id}")
  public Response<Void> updateRoom(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable Integer id,
      @RequestBody @Valid RoomRequest request) {
    roomService.update(userPrincipal.orgId(), id, request);
    return Response.ok();
  }

  @DeleteMapping("/{id}")
  public Response<Void> deleteRoom(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Integer id) {
    roomService.delete(userPrincipal.orgId(), id);
    return Response.ok();
  }

  @GetMapping
  public Response<Page<RoomResponse>> getAllRooms(
      @AuthenticationPrincipal UserPrincipal userPrincipal, Pageable pageable) {
    return Response.ok(roomService.getAll(userPrincipal.orgId(), pageable));
  }

  @GetMapping("/all")
  public Response<List<RoomResponse>> getAllRoomsList(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return Response.ok(roomService.findAll(userPrincipal.orgId()));
  }
}
