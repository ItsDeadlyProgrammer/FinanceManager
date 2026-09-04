package com.example.finance.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class RegisterRequest(
    @field:NotBlank(message = "Username/Email cannot be blank")
    @field:Email(message = "Username must be a valid email address")
    val username: String = "",

    @field:NotBlank(message = "Password cannot be blank")
    val password: String = "",

    @field:NotBlank(message = "Full name cannot be blank")
    val fullName: String = "",

    val phoneNumber: String? = null
)

data class LoginRequest(
    @field:NotBlank(message = "Username/Email cannot be blank")
    val username: String = "",

    @field:NotBlank(message = "Password cannot be blank")
    val password: String = ""
)

data class RegisterResponse(
    val message: String = "User registered successfully",
    val userId: Long = 0
)

data class LoginResponse(
    val message: String = "Login successful"
)

data class MessageResponse(
    val message: String = ""
)
