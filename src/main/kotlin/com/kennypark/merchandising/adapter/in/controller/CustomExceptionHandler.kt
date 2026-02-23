package com.kennypark.merchandising.adapter.`in`.controller

import com.kennypark.merchandising.domain.CustomException
import com.kennypark.merchandising.domain.ErrorVo
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.net.BindException

@ControllerAdvice(annotations = [Controller::class],
    basePackageClasses = [MerchandisingController::class])
class CustomExceptionHandler {
    @ExceptionHandler(CustomException::class)
    protected fun handleCustomException(e: CustomException): ResponseEntity<ErrorVo> {
        return ResponseEntity
            .status(e.errorVo.code.toInt())
            .body(e.errorVo)
    }

    @ExceptionHandler(BindException::class)
    protected fun handleBindException(e: BindException): ResponseEntity<ErrorVo> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorVo(
                HttpStatus.BAD_REQUEST.toString(),
                e.localizedMessage
            ))
    }
}