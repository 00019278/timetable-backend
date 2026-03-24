package com.sarmich.timetable.config;

import com.sarmich.timetable.exp.BadRequestException;
import com.sarmich.timetable.exp.ItemNotFoundException;
import com.sarmich.timetable.utils.Error;
import com.sarmich.timetable.utils.ResponseModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.Collections;

@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseModel> handleValidationException(MethodArgumentNotValidException ex) {
        var result = ex.getBindingResult();
        var errors = result.getFieldErrors();
        var messages = new ArrayList<String>();
        for (var error : errors) {
            messages.add(error.getDefaultMessage() != null ? error.getDefaultMessage() : "");
        }
        var response = new ResponseModel(
                false,
                new Error(messages, 400), null,
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ResponseModel> handleItemNotFoundException(ItemNotFoundException e) {
        var response = new ResponseModel(
                false,
                new Error(Collections.singletonList(e.getMessage()), 404), null,
                HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseModel> handleBadRequestException(BadRequestException e) {
        var response = new ResponseModel(
                false,
                new Error(Collections.singletonList(e.getMessage()), 400), null,
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseModel> handleRuntimeException(RuntimeException e) {
        var response = new ResponseModel(
                false,
                new Error(Collections.singletonList(e.getMessage()), 400), null,
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
