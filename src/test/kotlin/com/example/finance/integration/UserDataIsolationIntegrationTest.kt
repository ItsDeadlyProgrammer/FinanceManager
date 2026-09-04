package com.example.finance.integration

import com.example.finance.dto.auth.RegisterRequest
import com.example.finance.dto.category.CategoryRequest
import com.example.finance.dto.goal.GoalRequest
import com.example.finance.dto.transaction.TransactionRequest
import com.example.finance.entity.CategoryType
import com.example.finance.service.AuthService
import com.example.finance.service.CategoryService
import com.example.finance.service.SavingsGoalService
import com.example.finance.service.TransactionService
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
class UserDataIsolationIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var transactionService: TransactionService

    @Autowired
    private lateinit var goalService: SavingsGoalService

    @Autowired
    private lateinit var categoryService: CategoryService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private var emailA: String = ""
    private var emailB: String = ""
    private var userAId: Long = 0
    private var userBId: Long = 0

    private var userBTransactionId: Long = 0
    private var userBGoalId: Long = 0

    @BeforeEach
    fun setUp() {
        val timestamp = System.currentTimeMillis()
        emailA = "usera_$timestamp@example.com"
        emailB = "userb_$timestamp@example.com"

        val userAReg = authService.register(RegisterRequest(emailA, "pass123", "User A"))
        val userBReg = authService.register(RegisterRequest(emailB, "pass123", "User B"))
        userAId = userAReg.userId
        userBId = userBReg.userId

        // Create transaction for User B
        val txB = transactionService.createTransaction(
            userBId,
            TransactionRequest(BigDecimal("100.00"), LocalDate.now(), "Salary", "User B Income")
        )
        userBTransactionId = txB.id

        // Create Goal for User B
        val goalB = goalService.createGoal(
            userBId,
            GoalRequest("User B Goal", BigDecimal("5000.00"), LocalDate.now().plusDays(30))
        )
        userBGoalId = goalB.id

        // Create Custom Category for User B
        categoryService.createCustomCategory(userBId, CategoryRequest("UserBCat", CategoryType.EXPENSE))
    }

    @Test
    fun `User A cannot update or delete User B transaction`() {
        val updatePayload = mapOf("amount" to 200.00, "description" to "Hacked")

        mockMvc.perform(
            put("/api/transactions/$userBTransactionId")
                .with(user(emailA).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePayload))
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            delete("/api/transactions/$userBTransactionId")
                .with(user(emailA).roles("USER"))
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `User A cannot get, update, or delete User B goal`() {
        mockMvc.perform(
            get("/api/goals/$userBGoalId")
                .with(user(emailA).roles("USER"))
        ).andExpect(status().isForbidden)

        val updatePayload = mapOf("targetAmount" to 10000.00)
        mockMvc.perform(
            put("/api/goals/$userBGoalId")
                .with(user(emailA).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePayload))
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            delete("/api/goals/$userBGoalId")
                .with(user(emailA).roles("USER"))
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `User A cannot delete User B custom category`() {
        mockMvc.perform(
            delete("/api/categories/UserBCat")
                .with(user(emailA).roles("USER"))
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `Unauthenticated requests return 401 Unauthorized`() {
        mockMvc.perform(get("/api/transactions"))
            .andExpect(status().isUnauthorized)

        mockMvc.perform(get("/api/goals"))
            .andExpect(status().isUnauthorized)

        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isUnauthorized)
    }
}
