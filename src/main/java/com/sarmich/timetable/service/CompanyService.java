package com.sarmich.timetable.service;

import com.sarmich.timetable.domain.CompanyEntity;
import com.sarmich.timetable.mapper.CompanyMapper;
import com.sarmich.timetable.model.request.CompanyRequest;
import com.sarmich.timetable.model.response.CompanyResponse;
import com.sarmich.timetable.repository.CompanyRepository;
import com.sarmich.timetable.utils.Util;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Log4j2
public class CompanyService {

  private final CompanyRepository companyRepository;
  private final BuildingService buildingService;

  public CompanyEntity create(CompanyRequest company, Integer userId) {
    log.debug("Creating company {}", company);
    CompanyEntity entity = new CompanyEntity();
    entity.setName(company.name());
    entity.setDescription(company.description());
    entity.setDaysOfWeek(company.daysOfWeek());
    entity.setPeriods(company.periods());
    entity.setCreatedBy(userId);
    CompanyEntity saved = companyRepository.save(entity);
    buildingService.createDefaultBuilding(saved.getId());
    return saved;
  }

  public void update(CompanyRequest request, Integer userId) {
    log.debug("Update company {}", request);
    CompanyEntity company = companyRepository.findByCreatedByAndDeletedFalse(userId);
    Util.checkNull(company, CompanyEntity.class);
    company.setDescription(request.description());
    company.setName(request.name());
    company.setDaysOfWeek(request.daysOfWeek());
    company.setPeriods(request.periods());
    companyRepository.save(company);
  }

  public CompanyResponse getByUsedId(Integer userId) {
    log.debug("Find company {}", userId);
    return CompanyMapper.INSTANCE.toResponse(
        companyRepository.findByCreatedByAndDeletedFalse(userId));
  }

  public CompanyResponse getByOrgID(Integer orgId) {
    log.debug("Find company {}", orgId);
    return CompanyMapper.INSTANCE.toResponse(companyRepository.findOrgById(orgId));
  }
}
