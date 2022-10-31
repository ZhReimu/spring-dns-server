package com.mrx.springdnsserver.exception;

import com.mrx.springdnsserver.model.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

/**
 * @author Mr.X
 * @since 2022-11-01 07:43
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> ConstraintViolationExceptionHandler(Throwable t) {
        return Result.fail(t);
    }

}
