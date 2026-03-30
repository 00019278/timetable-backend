package com.sarmich.timetable.domain;

import com.sarmich.timetable.domain.enums.ProfileRole;
import com.sarmich.timetable.utils.Constants;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(schema = Constants.SCHEMA, name = Constants.TABLE_USER)
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String name;
  private String surname;
  private String email;
  private String phone;
  private String photo;
  private String password;

  @Enumerated(EnumType.STRING)
  private ProfileRole role = ProfileRole.ROLE_USER;

  @CreatedDate private Instant createdDate = Instant.now();
  @LastModifiedDate private Instant updatedDate = Instant.now();
  private Boolean deleted = Boolean.FALSE;
}
