package com.sarmich.timetable.model;

import java.util.UUID;

public record GenerateReply(UUID taskId, Integer orgId, String name) {}
