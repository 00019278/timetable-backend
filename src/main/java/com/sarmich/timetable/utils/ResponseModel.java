package com.sarmich.timetable.utils;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ResponseModel {

    private boolean isSuccess;
    private Error error;
    private Object data;
    private Integer httpStatus;
}