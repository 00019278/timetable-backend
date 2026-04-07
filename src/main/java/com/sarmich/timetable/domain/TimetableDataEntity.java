package com.sarmich.timetable.domain;

import com.sarmich.timetable.model.TimetableGroupDetail;
import com.sarmich.timetable.model.UnscheduledLesson;
import com.sarmich.timetable.utils.Constants;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Table(schema = Constants.SCHEMA, name = Constants.TABLE_TIMETABLE_DATA)
@EntityListeners(AuditingEntityListener.class)
public class TimetableDataEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private UUID timetableId;

  // Bu qator rejalashtirilgan slotmi yoki unscheduled (sig'magan) darsmi?
  private Boolean isScheduled = Boolean.TRUE;

  // Asosiy bog'lamalar (Faqat Sinf darajasida qoladi)
  private Integer classId;

  // teacherId, subjectId, roomId, groupId -> OLIB TASHLANDI (JSON ichida bo'ladi)

  @Enumerated(EnumType.STRING)
  private DayOfWeek dayOfWeek;

  private Integer hour;
  private Integer weekIndex; // 0, 1 yoki null

  // --- ENG MUHIM O'ZGARISH ---
  // Slot ichidagi barcha darslar ro'yxati shu yerda saqlanadi.
  @JdbcTypeCode(SqlTypes.JSON)
  private List<TimetableGroupDetail> slotDetails;

  // Unscheduled darslar uchun (agar isScheduled = false bo'lsa)
  // Bu ro'yxat bo'lishi ham mumkin, yoki bitta dona object
  @JdbcTypeCode(SqlTypes.JSON)
  private UnscheduledLesson unscheduledData;

  private Integer version = 1;
}
