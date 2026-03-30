package com.sarmich.timetable.domain;

import com.sarmich.timetable.model.LessonPeriod;
import com.sarmich.timetable.model.TimeSlot;
import com.sarmich.timetable.utils.Constants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = Constants.TABLE_ORG)
@EntityListeners(AuditingEntityListener.class)
public class CompanyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String name;
  private String description;

  @JdbcTypeCode(SqlTypes.JSON)
  private List<DayOfWeek> daysOfWeek;

  @JdbcTypeCode(SqlTypes.JSON)
  private List<LessonPeriod> periods;

  @CreatedBy Integer createdBy;
  @CreatedDate private Instant createdDate;
  @LastModifiedDate private Instant lastModifiedDate;
}
