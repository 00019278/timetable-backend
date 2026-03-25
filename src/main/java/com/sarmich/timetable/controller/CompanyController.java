package com.sarmich.timetable.controller;

import com.sarmich.timetable.model.request.CompanyRequest;
import com.sarmich.timetable.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("company")
@AllArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public void create(@RequestBody CompanyRequest company) {
        companyService.create(company);
    }
}
