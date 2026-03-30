package com.sarmich.timetable.model;

import java.time.Instant;

public record SmsCache(String email, Integer code, Instant time) {}
