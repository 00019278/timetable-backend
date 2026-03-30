package com.sarmich.timetable.model.request;

import com.sarmich.timetable.model.TimeSlot;

import java.util.List;

public record ClassRequest(String name, String shortName, List<TimeSlot> availabilities) {}
