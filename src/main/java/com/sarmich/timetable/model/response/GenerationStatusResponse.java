package com.sarmich.timetable.model.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerationStatusResponse {
  private UUID taskId;
  private String status; // "SUCCESS", "ERROR", "PROCESSING"
  private String message;
  private UUID timetableId; // Only if success
}
