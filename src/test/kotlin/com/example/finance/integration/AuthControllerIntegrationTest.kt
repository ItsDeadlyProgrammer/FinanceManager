package com.example.finance.integration

import com.example.finance.dto.auth.LoginRequest
import com.example.finance.dto.auth.RegisterRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `full auth flow - register, login, logout`() {
        val email = "newuser_${System.currentTimeMillis()}@example.com"
        val regReq = RegisterRequest(email, "Password123!", "Jane Doe", "+9876543210")

        // Register
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.message").value("User registered successfully"))
            .andExpect(jsonPath("$.userId").exists())

        // Duplicate Register -> 409
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq))
        )
            .andExpect(status().isConflict)

        // Login
        val loginReq = LoginRequest(email, "Password123!")
        val result = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Login successful"))
            .andReturn()

        val session = result.request.session!!

        // Logout
        mockMvc.perform(
            post("/api/auth/logout")
                .session(session as org.springframework.mock.web.MockHttpSession)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Logout successful"))
    }
}
