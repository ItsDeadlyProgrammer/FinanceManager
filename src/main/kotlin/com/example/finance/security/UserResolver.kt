package com.example.finance.security

import com.example.finance.exception.UnauthorizedException
import com.example.finance.repository.UserRepository
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class UserResolver(
    private val userRepository: UserRepository
) {
    fun resolveUserId(authentication: Authentication?): Long {
        if (authentication == null || !authentication.isAuthenticated) {
            throw UnauthorizedException("Authentication required")
        }
        val principal = authentication.principal
        if (principal is CustomUserDetails) {
            return principal.id
        }
        if (principal is UserDetails) {
            val user = userRepository.findByUsername(principal.username)
                ?: throw UnauthorizedException("User not found: ${principal.username}")
            return user.id!!
        }
        if (principal is String) {
            val user = userRepository.findByUsername(principal)
                ?: throw UnauthorizedException("User not found: $principal")
            return user.id!!
        }
        val name = authentication.name
        val user = userRepository.findByUsername(name)
            ?: throw UnauthorizedException("User not found: $name")
        return user.id!!
    }
}
