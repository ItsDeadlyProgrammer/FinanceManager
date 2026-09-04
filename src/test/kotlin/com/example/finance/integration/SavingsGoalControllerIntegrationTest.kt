package com.example.finance.integration

import com.example.finance.dto.auth.RegisterRequest
import com.example.finance.dto.goal.GoalRequest
import com.example.finance.dto.goal.GoalUpdateRequest
import com.example.finance.service.AuthService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SavingsGoalControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val userEmail = "goaltest_${System.currentTimeMillis()}@example.com"

    @BeforeEach
    fun setUp() {
        authService.register(RegisterRequest(userEmail, "Password123!", "Goal Tester"))
    }

    @Test
    fun `create, get, update, delete goal flow`() {
        val targetDate = LocalDate.now().plusDays(60)
        val startDate = LocalDate.now().minusDays(5)
        val goalReq = GoalRequest(
            goalName = "Emergency Fund",
            targetAmount = BigDecimal("5000.00"),
            targetDate = targetDate,
            startDate = startDate
        )

        val createResult = mockMvc.perform(
            post("/api/goals")
                .with(user(userEmail).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(goalReq))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.goalName").value("Emergency Fund"))
            .andExpect(jsonPath("$.targetAmount").value(5000.00))
            .andReturn()

        val responseStr = createResult.response.contentAsString
        val goalId = objectMapper.readTree(responseStr).get("id").asLong()

        mockMvc.perform(
            get("/api/goals")
                .with(user(userEmail).roles("USER"))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.goals[0].id").value(goalId))

        mockMvc.perform(
            get("/api/goals/$goalId")
                .with(user(userEmail).roles("USER"))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.goalName").value("Emergency Fund"))

        val updateReq = GoalUpdateRequest(targetAmount = BigDecimal("6000.00"))
        mockMvc.perform(
            put("/api/goals/$goalId")
                .with(user(userEmail).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.targetAmount").value(6000.00))

        mockMvc.perform(
            delete("/api/goals/$goalId")
                .with(user(userEmail).roles("USER"))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Goal deleted successfully"))
    }
}
