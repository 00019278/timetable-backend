package com.sarmich.timetable.domain;

import com.sarmich.timetable.model.LessonPeriod;
import com.sarmich.timetable.utils.Constants;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Table(schema = Constants.SCHEMA, name = Constants.TABLE_ORG)
@EntityListeners(AuditingEntityListener.class)
public class CompanyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String name;
  private String description;

  @JdbcTypeCode(SqlTypes.JSON)
  private Set<DayOfWeek> daysOfWeek;

  @JdbcTypeCode(SqlTypes.JSON)
  private List<LessonPeriod> periods;

  private Boolean deleted = Boolean.FALSE;

  @CreatedBy Integer createdBy;
  @CreatedDate private Instant createdDate = Instant.now();
  @LastModifiedDate private Instant lastModifiedDate = Instant.now();
}
