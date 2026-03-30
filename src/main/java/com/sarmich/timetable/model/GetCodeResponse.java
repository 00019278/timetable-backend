package com.sarmich.timetable.model;

import java.time.Instant;

public record GetCodeResponse(String email, Instant time) {}
