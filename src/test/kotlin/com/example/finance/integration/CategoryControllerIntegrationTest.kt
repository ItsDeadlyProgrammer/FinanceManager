package com.example.finance.integration

import com.example.finance.dto.auth.RegisterRequest
import com.example.finance.dto.category.CategoryRequest
import com.example.finance.entity.CategoryType
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val userEmail = "cattest_${System.currentTimeMillis()}@example.com"

    @BeforeEach
    fun setUp() {
        authService.register(RegisterRequest(userEmail, "Password123!", "Cat Tester"))
    }

    @Test
    fun `get default categories, create custom, delete custom`() {
        mockMvc.perform(
            get("/api/categories")
                .with(user(userEmail).roles("USER"))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.categories").isArray)
            .andExpect(jsonPath("$.categories[?(@.name == 'Salary')].custom").value(false))

        val catReq = CategoryRequest("SideBusinessIncome", CategoryType.INCOME)
        mockMvc.perform(
            post("/api/categories")
                .with(user(userEmail).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catReq))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("SideBusinessIncome"))
            .andExpect(jsonPath("$.custom").value(true))

        mockMvc.perform(
            post("/api/categories")
                .with(user(userEmail).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catReq))
        )
            .andExpect(status().isConflict)

        mockMvc.perform(
            delete("/api/categories/Salary")
                .with(user(userEmail).roles("USER"))
        )
            .andExpect(status().isBadRequest)

        mockMvc.perform(
            delete("/api/categories/SideBusinessIncome")
                .with(user(userEmail).roles("USER"))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Category deleted successfully"))
    }
}
