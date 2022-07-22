package com.example.simplecrud.controller.advice;

import com.example.simplecrud.Domain.Dto.errorDto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ControllerAdvice("com.example.simplecrud.controller.BoardController")
public class boardExceptionAdvisor { //컨트롤러 파일을 패키지로 나눔, controlleradvice를 패키지 별로 적용하기 위함.

    @ExceptionHandler(BindException.class)
    public ResponseEntity<List<ErrorResponse>> SignUpError(BindException exception, HttpServletRequest request){
        log.info(request.getRequestURI());
        List<ErrorResponse> list = makeErrorResponse(exception.getBindingResult());
        return new ResponseEntity<>(list,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> DuplicateError(RuntimeException exception){
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage());
        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);

    }


    private List<ErrorResponse> makeErrorResponse(BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            List<ErrorResponse> list = new ArrayList<>();
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            for (ObjectError allError : allErrors) {
                list.add(new ErrorResponse(allError.getDefaultMessage()));
            }
            return list;
        }
        return null;
    }



}
