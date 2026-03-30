package com.sarmich.timetable.service;

import com.sarmich.timetable.model.request.CompanyRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CompanyService {

    private final com.sarmich.timetable.repository.CompanyRepository companyRepository;

    public void create(CompanyRequest company) {
        com.sarmich.timetable.domain.CompanyEntity entity = new com.sarmich.timetable.domain.CompanyEntity();
        entity.setName(company.name());
        entity.setDescription(company.description());
        entity.setDaysOfWeek(company.daysOfWeek());
        entity.setPeriods(company.periods());
        companyRepository.save(entity);
    }
}
