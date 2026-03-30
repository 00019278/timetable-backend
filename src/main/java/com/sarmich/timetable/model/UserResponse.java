package com.sarmich.timetable.model;

// @JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
    Long id, String image, String firstName, String lastName, Boolean deleted) {}
