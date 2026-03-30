package com.sarmich.timetable.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = Constants.TABLE_CLASS)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer orgId;

    private String name;
    private String shortName;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<TimeSlot> availabilities;

    @CreatedBy
    private Integer createdBy;
    private Boolean deleted = Boolean.FALSE;
    @CreatedDate
    private Instant createdDate = Instant.now();
    @LastModifiedDate
    private Instant updatedDate = Instant.now();
}
