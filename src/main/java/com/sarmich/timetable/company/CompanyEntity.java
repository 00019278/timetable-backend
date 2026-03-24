package com.sarmich.timetable.company;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "company")
public class CompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant startTime;
    private Instant timeLesson;
    private Instant breakTime;
    private Integer maxLesson;}