package com.sarmich.timetable.domain;

import com.sarmich.timetable.utils.Constants;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Table(schema = Constants.SCHEMA, name = Constants.TABLE_TASKS)
@EntityListeners(AuditingEntityListener.class)
public class TaskEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private Integer orgId;
  private String message;
  private Boolean isSuccess = Boolean.FALSE;
  private Boolean isFinished = Boolean.FALSE;
  @CreatedBy private Integer createdBy;
  @CreatedDate private Instant createdDate = Instant.now();
  @LastModifiedDate private Instant updatedDate = Instant.now();
}
