package com.sarmich.timetable.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarmich.timetable.model.GenerateReply;
import com.sarmich.timetable.service.TimetableService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@AllArgsConstructor
public class Subscriber {
  private final ObjectMapper objectMapper;
  private final TimetableService service;

  public void onMessage(String message, String channel) throws Exception {
    log.info("📥 Received message: '{}' from channel: {}", message, channel);
    GenerateReply reply = objectMapper.readValue(message, GenerateReply.class);
    service.generateTimetable(reply);
  }
}
