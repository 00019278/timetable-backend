package com.sarmich.timetable.domain;

import com.sarmich.timetable.utils.Constants;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Table(schema = Constants.SCHEMA, name = Constants.TABLE_GROUP)
@EntityListeners(AuditingEntityListener.class)
public class GroupEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer orgId;
  private Integer classId;
  private String name;

  @CreatedBy private Integer createdBy;
  private Boolean deleted = Boolean.FALSE;
  @CreatedDate private Instant createdDate = Instant.now();
  @LastModifiedDate private Instant updatedDate = Instant.now();
}
