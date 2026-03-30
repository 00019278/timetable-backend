package com.sarmich.timetable.domain;

import com.sarmich.timetable.model.TimeSlot;
import com.sarmich.timetable.utils.Constants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Table(name = Constants.TABLE_TEACHER)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
public class TeacherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer orgId;
    private String fullName;
    private String shortName;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<TimeSlot> availabilities;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Integer> subjects;

    private boolean deleted = false;
    @CreatedDate
    private Instant createdDate = Instant.now();
    @LastModifiedDate
    private Instant updatedDate = Instant.now();
}
