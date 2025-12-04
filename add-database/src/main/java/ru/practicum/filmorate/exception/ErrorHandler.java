package ru.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleNotFound(NotFoundException e) {
        Map<String,Object> body = new HashMap<String,Object>();
        body.put("error", e.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return new ResponseEntity<Map<String,Object>>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(ValidationException e) {
        Map<String,Object> body = new HashMap<String,Object>();
        body.put("error", e.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return new ResponseEntity<Map<String,Object>>(body, HttpStatus.BAD_REQUEST);
    }
}

