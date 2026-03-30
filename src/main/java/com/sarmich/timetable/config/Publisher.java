package com.sarmich.timetable.config;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class Publisher {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ChannelTopic topic;

  public Publisher(RedisTemplate<String, Object> redisTemplate, ChannelTopic topic) {
    this.redisTemplate = redisTemplate;
    this.topic = topic;
  }

  public <T> void send(T message) {
    redisTemplate.convertAndSend(topic.getTopic(), message);
    System.out.println("📤 Sent: " + message);
  }
}
