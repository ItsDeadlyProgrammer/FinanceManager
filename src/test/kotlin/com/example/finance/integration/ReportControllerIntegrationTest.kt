package com.example.finance.integration

import com.example.finance.dto.auth.RegisterRequest
import com.example.finance.dto.transaction.TransactionRequest
import com.example.finance.service.AuthService
import com.example.finance.service.TransactionService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var transactionService: TransactionService

    private val userEmail = "reporttest_${System.currentTimeMillis()}@example.com"

    @BeforeEach
    fun setUp() {
        val reg = authService.register(RegisterRequest(userEmail, "Password123!", "Report Tester"))

        transactionService.createTransaction(
            reg.userId,
            TransactionRequest(BigDecimal("3000.00"), LocalDate.of(2026, 1, 10), "Salary", "Jan Salary")
        )
        transactionService.createTransaction(
            reg.userId,
            TransactionRequest(BigDecimal("400.00"), LocalDate.of(2026, 1, 15), "Food", "Groceries")
        )
    }

    @Test
    fun `monthly report endpoint`() {
        mockMvc.perform(
            get("/api/reports/monthly/2026/1")
                .with(user(userEmail).roles("USER"))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.year").value(2026))
            .andExpect(jsonPath("$.month").value(1))
            .andExpect(jsonPath("$.totalIncome.Salary").value(3000.00))
            .andExpect(jsonPath("$.totalExpenses.Food").value(400.00))
            .andExpect(jsonPath("$.netSavings").value(2600.00))
    }

    @Test
    fun `yearly report endpoint`() {
        mockMvc.perform(
            get("/api/reports/yearly/2026")
                .with(user(userEmail).roles("USER"))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.year").value(2026))
            .andExpect(jsonPath("$.netSavings").value(2600.00))
    }
}
