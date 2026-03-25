package com.sarmich.timetable.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Table(name = "teacher")
@Entity
@Getter
@Setter
public class TeacherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;
    private Integer profileId;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> lessonTimes = new HashMap<>();

    private boolean deleted=false;

    @CreatedDate
    private Instant createdDate=Instant.now();

    @LastModifiedDate
    private Instant updatedDate=Instant.now();
}
