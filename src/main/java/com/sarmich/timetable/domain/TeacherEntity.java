package com.sarmich.timetable.domain;

import com.sarmich.timetable.model.TimeSlot;
import com.sarmich.timetable.utils.Constants;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@EntityListeners(AuditingEntityListener.class)
@Table(schema = Constants.SCHEMA, name = Constants.TABLE_TEACHER)
public class TeacherEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer orgId;
  private String fullName;
  private String shortName;

  @JdbcTypeCode(SqlTypes.JSON)
  private List<TimeSlot> availabilities;

  private boolean deleted = false;
  @CreatedDate private Instant createdDate = Instant.now();
  @LastModifiedDate private Instant updatedDate = Instant.now();
}
