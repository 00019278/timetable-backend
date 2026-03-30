package com.sarmich.timetable.api;

import com.sarmich.timetable.model.UserPrincipal;
import com.sarmich.timetable.model.common.Response;
import com.sarmich.timetable.model.request.CompanyRequest;
import com.sarmich.timetable.model.response.CompanyResponse;
import com.sarmich.timetable.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/company")
@AllArgsConstructor
public class CompanyController {

  private final CompanyService companyService;

  @PutMapping()
  public Response<Void> put(
      @AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody CompanyRequest request) {
    companyService.update(request, userPrincipal.user().id().intValue());
    return Response.ok();
  }

  @GetMapping()
  public Response<CompanyResponse> findById(@AuthenticationPrincipal UserPrincipal userPrincipal) {
    return Response.ok(companyService.getByUsedId(userPrincipal.user().id().intValue()));
  }
}
