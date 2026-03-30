package com.sarmich.timetable.domain;

import com.sarmich.timetable.utils.Constants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = Constants.TABLE_LESSON)
@EntityListeners(AuditingEntityListener.class)

@Getter
@Setter
public class LessonEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer orgId;
    private Integer subjectId;
    private Integer teacherId;
    private Integer classId;
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Integer> roomIds;
    private Integer period; // single,double,triple
    private Integer lessonCount;
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
    private Integer hour;
    private Boolean deleted = false;
    @CreatedDate
    private Instant createdDate = Instant.now();
    @LastModifiedDate
    private Instant updatedDate = Instant.now();
}
