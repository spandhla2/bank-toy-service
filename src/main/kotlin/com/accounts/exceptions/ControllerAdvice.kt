package com.accounts.exceptions

import com.accounts.api.BankAccountController
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.lang.IllegalArgumentException
import java.time.LocalDateTime
import java.time.LocalDateTime.now

@RestControllerAdvice(
    assignableTypes = [
        BankAccountController::class
    ]
)
class ControllerAdvice {

    @ExceptionHandler(Exception::class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    fun handleAllExceptions(ex: RuntimeException): ResponseEntity<ErrorMessage> {
        return ResponseEntity(ErrorMessage(ex.message, INTERNAL_SERVER_ERROR.value()), INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(BankAccountNotFoundException::class)
    @ResponseStatus(NOT_FOUND)
    fun handleNotFound(ex: RuntimeException): ResponseEntity<ErrorMessage> {
        return ResponseEntity(ErrorMessage(ex.message, NOT_FOUND.value()), NOT_FOUND)
    }

    @ExceptionHandler(
        AccountOperationException::class,
        MethodArgumentTypeMismatchException::class,
        InvalidFormatException::class
    )
    @ResponseStatus(BAD_REQUEST)
    fun handleBadRequest(ex: RuntimeException): ResponseEntity<ErrorMessage> {
        return ResponseEntity(ErrorMessage(ex.message, BAD_REQUEST.value()), BAD_REQUEST)
    }
}

data class ErrorMessage(
    val message: String?,
    val statusCode: Int,
    val timestamp: LocalDateTime = now()
)
