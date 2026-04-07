package com.sarmich.timetable.api;

import com.sarmich.timetable.model.UserPrincipal;
import com.sarmich.timetable.model.common.Response;
import com.sarmich.timetable.model.request.GenerateTimetableRequest;
import com.sarmich.timetable.model.response.TimetableFullResponse;
import com.sarmich.timetable.model.response.TimetableGenerateResponse;
import com.sarmich.timetable.model.response.TimetableResponse;
import com.sarmich.timetable.service.TimetableExportService;
import com.sarmich.timetable.service.TimetablePdfExportService;
import com.sarmich.timetable.service.TimetableService;
import com.sarmich.timetable.service.solver.ApplySoftConstraint;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timetable/v1/timetable")
@AllArgsConstructor
public class TimetableController {

  private final TimetableService service;
  private final TimetableExportService exportService;
  private final TimetablePdfExportService pdfExportService;

  @PostMapping("/generate")
  public Response<TimetableGenerateResponse> generate(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestBody GenerateTimetableRequest request) {
    return Response.ok(service.generate(userPrincipal.orgId(), request));
  }

  @GetMapping()
  public Response<List<TimetableResponse>> findAll(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    return Response.ok(service.find(userPrincipal.orgId()));
  }

  @GetMapping("/{id}")
  public Response<TimetableFullResponse> findAll(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable UUID id) {
    return Response.ok(service.findById(id, userPrincipal.orgId()));
  }

  @PostMapping("/optimize/{id}")
  public Response<Void> optimize(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @PathVariable UUID id,
      @RequestBody ApplySoftConstraint request) {
    service.optimize(id, request);
    return Response.ok();
  }

  @DeleteMapping("/{id}")
  public Response<Void> delete(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable UUID id) {
    service.delete(id, userPrincipal.orgId());
    return Response.ok();
  }

  @GetMapping("/export/{id}")
  public ResponseEntity<byte[]> exportToExcel(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable UUID id)
      throws IOException {
    byte[] excelContent = exportService.exportToExcel(id);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=timetable.xlsx")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(excelContent);
  }

  @GetMapping("/export/pdf/{id}")
  public ResponseEntity<byte[]> exportToPdf(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable UUID id)
      throws IOException {
    byte[] pdfContent = pdfExportService.exportToPdf(id);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=timetable.pdf")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdfContent);
  }
}
