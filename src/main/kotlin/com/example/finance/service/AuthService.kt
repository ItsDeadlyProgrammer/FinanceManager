package com.example.finance.service

import com.example.finance.dto.auth.LoginRequest
import com.example.finance.dto.auth.LoginResponse
import com.example.finance.dto.auth.RegisterRequest
import com.example.finance.dto.auth.RegisterResponse
import com.example.finance.entity.User
import com.example.finance.exception.BadRequestException
import com.example.finance.exception.ResourceAlreadyExistsException
import com.example.finance.exception.UnauthorizedException
import com.example.finance.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager
) {
    private val securityContextRepository: SecurityContextRepository = HttpSessionSecurityContextRepository()

    @Transactional
    fun register(request: RegisterRequest): RegisterResponse {
        if (request.username.isBlank()) {
            throw BadRequestException("Username/Email cannot be blank")
        }
        if (userRepository.existsByUsername(request.username)) {
            throw ResourceAlreadyExistsException("User with username ${request.username} already exists")
        }

        val user = User(
            username = request.username.trim(),
            passwordHash = passwordEncoder.encode(request.password),
            fullName = request.fullName.trim(),
            phoneNumber = request.phoneNumber?.trim()
        )
        val savedUser = userRepository.save(user)
        return RegisterResponse(
            message = "User registered successfully",
            userId = savedUser.id!!
        )
    }

    fun login(
        request: LoginRequest,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse
    ): LoginResponse {
        try {
            val authToken = UsernamePasswordAuthenticationToken(request.username.trim(), request.password)
            val authentication = authenticationManager.authenticate(authToken)

            val context = SecurityContextHolder.createEmptyContext()
            context.authentication = authentication
            SecurityContextHolder.setContext(context)
            securityContextRepository.saveContext(context, httpRequest, httpResponse)

            return LoginResponse(message = "Login successful")
        } catch (ex: BadCredentialsException) {
            throw UnauthorizedException("Invalid username or password")
        }
    }
}
