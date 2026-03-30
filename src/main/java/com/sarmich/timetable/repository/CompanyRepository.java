package com.sarmich.timetable.repository;

import com.sarmich.timetable.domain.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Integer> {
  CompanyEntity findByCreatedByAndDeletedFalse(Integer id);

  @Query(" from CompanyEntity where id = ?1")
  CompanyEntity findOrgById(Integer id);
}
