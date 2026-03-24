package com.sarmich.timetable.teacher;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@Table(name = "teacher")
@Entity
public class TeacherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String lastName;

    private Long subjectId;

    private Integer profileId;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> timeSlots = new HashMap<>();

    @UpdateTimestamp
    private Instant updatedDate;

    private boolean deleted;

    @CreationTimestamp
    private Instant createdDate;

    // Constructors, getters, setters, etc.
}
