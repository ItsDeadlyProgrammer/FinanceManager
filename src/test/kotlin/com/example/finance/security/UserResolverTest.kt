package com.example.finance.security

import com.example.finance.entity.User
import com.example.finance.exception.UnauthorizedException
import com.example.finance.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User as SpringUser

class UserResolverTest {

    private val userRepository: UserRepository = mockk()
    private lateinit var userResolver: UserResolver

    @BeforeEach
    fun setUp() {
        userResolver = UserResolver(userRepository)
    }

    @Test
    fun `null or unauthenticated throws UnauthorizedException`() {
        assertThrows(UnauthorizedException::class.java) {
            userResolver.resolveUserId(null)
        }
        val unauth = mockk<Authentication>()
        every { unauth.isAuthenticated } returns false
        assertThrows(UnauthorizedException::class.java) {
            userResolver.resolveUserId(unauth)
        }
    }

    @Test
    fun `CustomUserDetails principal resolves ID directly`() {
        val userDetails = CustomUserDetails(42L, "user@example.com", "hash")
        val auth = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)

        val userId = userResolver.resolveUserId(auth)

        assertEquals(42L, userId)
    }

    @Test
    fun `Spring UserDetails principal resolves via repository lookup`() {
        val springUser = SpringUser.builder()
            .username("spring@example.com")
            .password("pass")
            .roles("USER")
            .build()
        val auth = UsernamePasswordAuthenticationToken(springUser, null, springUser.authorities)

        val dbUser = User(id = 99L, username = "spring@example.com", passwordHash = "hash", fullName = "Spring User")
        every { userRepository.findByUsername("spring@example.com") } returns dbUser

        val userId = userResolver.resolveUserId(auth)

        assertEquals(99L, userId)
    }

    @Test
    fun `String principal resolves via repository lookup`() {
        val auth = UsernamePasswordAuthenticationToken("stringuser@example.com", null, emptyList())
        val dbUser = User(id = 88L, username = "stringuser@example.com", passwordHash = "hash", fullName = "String User")

        every { userRepository.findByUsername("stringuser@example.com") } returns dbUser

        val userId = userResolver.resolveUserId(auth)

        assertEquals(88L, userId)
    }

    @Test
    fun `unknown principal throws UnauthorizedException`() {
        val springUser = SpringUser.builder()
            .username("unknown@example.com")
            .password("pass")
            .roles("USER")
            .build()
        val auth = UsernamePasswordAuthenticationToken(springUser, null, springUser.authorities)

        every { userRepository.findByUsername("unknown@example.com") } returns null

        assertThrows(UnauthorizedException::class.java) {
            userResolver.resolveUserId(auth)
        }
    }
}
