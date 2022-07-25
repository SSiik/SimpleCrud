package com.example.simplecrud.controller.advice;


import com.example.simplecrud.Domain.Dto.errorDto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@ControllerAdvice("com.example.simplecrud.Interceptor")
public class InterceptorExceptionAdvisor {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> InterceptorError(RuntimeException exception){
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage());
        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);

    }
}
