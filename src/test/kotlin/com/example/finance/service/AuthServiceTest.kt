package com.example.finance.service

import com.example.finance.dto.auth.LoginRequest
import com.example.finance.dto.auth.RegisterRequest
import com.example.finance.entity.User
import com.example.finance.exception.BadRequestException
import com.example.finance.exception.ResourceAlreadyExistsException
import com.example.finance.exception.UnauthorizedException
import com.example.finance.repository.UserRepository
import io.mockk.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder

class AuthServiceTest {

    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val authenticationManager: AuthenticationManager = mockk()

    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        authService = AuthService(userRepository, passwordEncoder, authenticationManager)
    }

    @Test
    fun `register success`() {
        val req = RegisterRequest("user@example.com", "password123", "John Doe", "+1234567890")
        every { userRepository.existsByUsername("user@example.com") } returns false
        every { passwordEncoder.encode("password123") } returns "hashed_pw"
        every { userRepository.save(any()) } answers {
            val u = firstArg<User>()
            u.id = 1L
            u
        }

        val res = authService.register(req)

        assertEquals(1L, res.userId)
        assertEquals("User registered successfully", res.message)
        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `register duplicate email throws ResourceAlreadyExistsException`() {
        val req = RegisterRequest("user@example.com", "password123", "John Doe")
        every { userRepository.existsByUsername("user@example.com") } returns true

        assertThrows(ResourceAlreadyExistsException::class.java) {
            authService.register(req)
        }
    }

    @Test
    fun `register blank username throws BadRequestException`() {
        val req = RegisterRequest("   ", "password123", "John Doe")
        assertThrows(BadRequestException::class.java) {
            authService.register(req)
        }
    }

    @Test
    fun `login success`() {
        val req = LoginRequest("user@example.com", "password123")
        val httpReq = mockk<HttpServletRequest>(relaxed = true)
        val httpRes = mockk<HttpServletResponse>(relaxed = true)
        val auth = mockk<Authentication>(relaxed = true)

        every { authenticationManager.authenticate(any<UsernamePasswordAuthenticationToken>()) } returns auth

        val res = authService.login(req, httpReq, httpRes)

        assertEquals("Login successful", res.message)
    }

    @Test
    fun `login incorrect password throws UnauthorizedException`() {
        val req = LoginRequest("user@example.com", "wrongpass")
        val httpReq = mockk<HttpServletRequest>(relaxed = true)
        val httpRes = mockk<HttpServletResponse>(relaxed = true)

        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("Bad creds")

        assertThrows(UnauthorizedException::class.java) {
            authService.login(req, httpReq, httpRes)
        }
    }
}
