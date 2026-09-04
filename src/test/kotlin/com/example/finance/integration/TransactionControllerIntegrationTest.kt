package com.example.finance.integration

import com.example.finance.dto.auth.RegisterRequest
import com.example.finance.dto.transaction.TransactionRequest
import com.example.finance.dto.transaction.TransactionUpdateRequest
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
class TransactionControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val userEmail = "txtest_${System.currentTimeMillis()}@example.com"

    @BeforeEach
    fun setUp() {
        authService.register(RegisterRequest(userEmail, "Password123!", "Tx Tester"))
    }

    @Test
    fun `create, get, update, delete transaction flow`() {
        val txReq = TransactionRequest(
            amount = BigDecimal("50000.00"),
            date = LocalDate.now(),
            category = "Salary",
            description = "January Salary"
        )

        val createResult = mockMvc.perform(
            post("/api/transactions")
                .with(user(userEmail).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(txReq))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.amount").value(50000.00))
            .andExpect(jsonPath("$.category").value("Salary"))
            .andExpect(jsonPath("$.type").value("INCOME"))
            .andReturn()

        val responseStr = createResult.response.contentAsString
        val txId = objectMapper.readTree(responseStr).get("id").asLong()

        mockMvc.perform(
            get("/api/transactions")
                .with(user(userEmail).roles("USER"))
                .param("type", "INCOME")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.transactions[0].id").value(txId))

        val updateReq = TransactionUpdateRequest(
            amount = BigDecimal("60000.00"),
            description = "Bonus included"
        )
        mockMvc.perform(
            put("/api/transactions/$txId")
                .with(user(userEmail).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.amount").value(60000.00))
            .andExpect(jsonPath("$.description").value("Bonus included"))

        mockMvc.perform(
            delete("/api/transactions/$txId")
                .with(user(userEmail).roles("USER"))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Transaction deleted successfully"))

        mockMvc.perform(
            get("/api/transactions")
                .with(user(userEmail).roles("USER"))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.transactions").isEmpty)
    }
}
