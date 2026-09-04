package com.example.finance.controller

import com.example.finance.dto.auth.LoginRequest
import com.example.finance.dto.auth.LoginResponse
import com.example.finance.dto.auth.MessageResponse
import com.example.finance.dto.auth.RegisterRequest
import com.example.finance.dto.auth.RegisterResponse
import com.example.finance.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/auth", "/auth"])
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and logout")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and establish session")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        val response = authService.login(request, httpRequest, httpResponse)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and invalidate session")
    fun logout(httpRequest: HttpServletRequest, httpResponse: HttpServletResponse): ResponseEntity<MessageResponse> {
        val session = httpRequest.getSession(false)
        session?.invalidate()
        SecurityContextHolder.clearContext()
        val cookie = jakarta.servlet.http.Cookie("JSESSIONID", "")
        cookie.maxAge = 0
        cookie.path = "/"
        httpResponse.addCookie(cookie)
        return ResponseEntity.ok(MessageResponse("Logout successful"))
    }
}
