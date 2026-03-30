package com.sarmich.timetable.api;

import com.sarmich.timetable.model.response.GenerationStatusResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

  @MessageMapping("/generation/{taskId}")
  @SendTo("/topic/generation/{taskId}")
  public GenerationStatusResponse subscribeToGeneration(
      @DestinationVariable UUID taskId, @Payload String message) {
    // This method can be used if the client needs to send a message to the server
    // For now, it just echoes back, but the main logic is in TimetableService
    return new GenerationStatusResponse(
        taskId, "CONNECTED", "Client connected to task " + taskId, null);
  }
}
