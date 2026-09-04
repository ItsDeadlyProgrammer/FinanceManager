package com.example.finance.service

import com.example.finance.entity.Category
import com.example.finance.entity.CategoryType
import com.example.finance.entity.Transaction
import com.example.finance.exception.BadRequestException
import com.example.finance.repository.TransactionRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class ReportServiceTest {

    private val transactionRepository: TransactionRepository = mockk()
    private lateinit var reportService: ReportService

    @BeforeEach
    fun setUp() {
        reportService = ReportService(transactionRepository)
    }

    @Test
    fun `getMonthlyReport success`() {
        val catSalary = Category(1L, "Salary", CategoryType.INCOME, null, false)
        val catFood = Category(2L, "Food", CategoryType.EXPENSE, null, false)

        val t1 = Transaction(1L, 10L, BigDecimal("3000.00"), LocalDate.of(2026, 1, 15), catSalary)
        val t2 = Transaction(2L, 10L, BigDecimal("400.00"), LocalDate.of(2026, 1, 20), catFood)

        every { transactionRepository.findAllByUserIdAndDateBetween(10L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)) } returns listOf(t1, t2)

        val res = reportService.getMonthlyReport(10L, 2026, 1)

        assertEquals(1, res.month)
        assertEquals(2026, res.year)
        assertEquals(BigDecimal("3000.00"), res.totalIncome["Salary"])
        assertEquals(BigDecimal("400.00"), res.totalExpenses["Food"])
        assertEquals(BigDecimal("2600.00"), res.netSavings)
    }

    @Test
    fun `getYearlyReport success`() {
        val catSalary = Category(1L, "Salary", CategoryType.INCOME, null, false)
        val catRent = Category(3L, "Rent", CategoryType.EXPENSE, null, false)

        val t1 = Transaction(1L, 10L, BigDecimal("50000.00"), LocalDate.of(2026, 6, 1), catSalary)
        val t2 = Transaction(2L, 10L, BigDecimal("12000.00"), LocalDate.of(2026, 6, 2), catRent)

        every { transactionRepository.findAllByUserIdAndDateBetween(10L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)) } returns listOf(t1, t2)

        val res = reportService.getYearlyReport(10L, 2026)

        assertEquals(2026, res.year)
        assertEquals(BigDecimal("50000.00"), res.totalIncome["Salary"])
        assertEquals(BigDecimal("12000.00"), res.totalExpenses["Rent"])
        assertEquals(BigDecimal("38000.00"), res.netSavings)
    }

    @Test
    fun `getMonthlyReport invalid month throws BadRequestException`() {
        assertThrows(BadRequestException::class.java) {
            reportService.getMonthlyReport(10L, 2026, 13)
        }
    }

    @Test
    fun `getYearlyReport invalid year throws BadRequestException`() {
        assertThrows(BadRequestException::class.java) {
            reportService.getYearlyReport(10L, 1800)
        }
    }
}
