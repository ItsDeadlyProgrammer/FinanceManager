package com.example.finance.security

import com.example.finance.entity.User
import com.example.finance.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.userdetails.UsernameNotFoundException

class CustomUserDetailsServiceTest {

    private val userRepository: UserRepository = mockk()
    private lateinit var service: CustomUserDetailsService

    @BeforeEach
    fun setUp() {
        service = CustomUserDetailsService(userRepository)
    }

    @Test
    fun `loadUserByUsername success`() {
        val user = User(id = 1L, username = "test@example.com", passwordHash = "hash", fullName = "Test User")
        every { userRepository.findByUsername("test@example.com") } returns user

        val details = service.loadUserByUsername("test@example.com")

        assertEquals("test@example.com", details.username)
        assertEquals("hash", details.password)
        assertTrue(details.isEnabled)
        assertTrue(details.isAccountNonExpired)
        assertTrue(details.isAccountNonLocked)
        assertTrue(details.isCredentialsNonExpired)
        assertEquals(1, details.authorities.size)
        assertEquals("ROLE_USER", details.authorities.first().authority)
    }

    @Test
    fun `loadUserByUsername user not found throws UsernameNotFoundException`() {
        every { userRepository.findByUsername("missing@example.com") } returns null

        assertThrows(UsernameNotFoundException::class.java) {
            service.loadUserByUsername("missing@example.com")
        }
    }
}
