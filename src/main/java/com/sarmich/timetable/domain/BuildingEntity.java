package com.sarmich.timetable.domain;

import com.sarmich.timetable.utils.Constants;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(schema = Constants.SCHEMA, name = Constants.TABLE_BUILDING)
public class BuildingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer orgId;
  private String name;
  private boolean isDefault = false;
  private Boolean deleted = Boolean.FALSE;
  @CreatedDate private Instant createdDate = Instant.now();
  @LastModifiedDate private Instant updatedDate = Instant.now();
}
