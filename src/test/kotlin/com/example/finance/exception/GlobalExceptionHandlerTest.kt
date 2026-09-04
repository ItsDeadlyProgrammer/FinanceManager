package com.example.finance.exception

import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleBadRequest`() {
        val res = handler.handleBadRequest(BadRequestException("Bad data"))
        assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
        assertEquals("Bad data", res.body?.message)
    }

    @Test
    fun `handleNotFound`() {
        val res = handler.handleNotFound(ResourceNotFoundException("Not found"))
        assertEquals(HttpStatus.NOT_FOUND, res.statusCode)
        assertEquals("Not found", res.body?.message)
    }

    @Test
    fun `handleForbidden`() {
        val res = handler.handleForbidden(ForbiddenAccessException("Forbidden"))
        assertEquals(HttpStatus.FORBIDDEN, res.statusCode)
        assertEquals("Forbidden", res.body?.message)
    }

    @Test
    fun `handleConflict`() {
        val res = handler.handleConflict(ResourceAlreadyExistsException("Already exists"))
        assertEquals(HttpStatus.CONFLICT, res.statusCode)
        assertEquals("Already exists", res.body?.message)
    }

    @Test
    fun `handleUnauthorized`() {
        val res = handler.handleUnauthorized(UnauthorizedException("Unauthorized"))
        assertEquals(HttpStatus.UNAUTHORIZED, res.statusCode)
        assertEquals("Unauthorized", res.body?.message)
    }

    @Test
    fun `handleHttpMessageNotReadable`() {
        val res = handler.handleHttpMessageNotReadable(HttpMessageNotReadableException("Invalid JSON"))
        assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
        assertEquals("Malformed JSON request", res.body?.message)
    }

    @Test
    fun `handleAccessDenied`() {
        val res = handler.handleAccessDenied(AccessDeniedException("Denied"))
        assertEquals(HttpStatus.FORBIDDEN, res.statusCode)
        assertEquals("Denied", res.body?.message)
    }

    @Test
    fun `handleGeneral`() {
        val res = handler.handleGeneral(RuntimeException("Fatal"))
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.statusCode)
        assertEquals("Fatal", res.body?.message)
    }
}
