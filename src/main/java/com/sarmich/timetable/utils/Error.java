package com.sarmich.timetable.utils;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;
@AllArgsConstructor
@RequiredArgsConstructor
public class Error {

    private Integer code;
    private List<String> error;

    public Error( List<String> error,Integer code) {
        this.code = code;
        this.error = error;
    }
}