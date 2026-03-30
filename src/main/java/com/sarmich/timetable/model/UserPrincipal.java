package com.sarmich.timetable.model;

public record UserPrincipal(UserResponse user, String token, Integer orgId) {}
