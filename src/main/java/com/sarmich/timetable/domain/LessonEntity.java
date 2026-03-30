package com.sarmich.timetable.domain;

import com.sarmich.timetable.model.request.GroupLessonDetail;
import com.sarmich.timetable.service.solver.LessonFrequency;
import com.sarmich.timetable.utils.Constants;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Table(schema = Constants.SCHEMA, name = Constants.TABLE_LESSON)
@EntityListeners(AuditingEntityListener.class)
public class LessonEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer orgId;
  private Integer subjectId;
  private Integer teacherId;
  private Integer classId;
  private Integer syncId;

  @JdbcTypeCode(SqlTypes.JSON)
  private List<Integer> roomIds;

  @JdbcTypeCode(SqlTypes.JSON)
  private List<GroupLessonDetail> groupDetails;

  private Integer period; // single,double,triple
  private Integer lessonCount;

  @Enumerated(EnumType.STRING)
  private LessonFrequency frequency;

  //    private Boolean isLecture;  // agar lecture bolsa 1 ta teacher 1 ta xonada 2 3 ta guruhga
  // dars otishi mumkin
  //    private Boolean isInTwoWeek; // agar shunday bolsa 1 hafta dars boladi 1 hafta bolmaydi
  //    private Boolean isDeletedGroup;// guruhlarga bolinadimi
  @Enumerated(EnumType.STRING)
  private DayOfWeek dayOfWeek;

  private Integer hour;
  private Boolean deleted = false;
  @CreatedDate private Instant createdDate = Instant.now();
  @LastModifiedDate private Instant updatedDate = Instant.now();
}
