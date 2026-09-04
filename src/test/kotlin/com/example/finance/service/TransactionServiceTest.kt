package com.example.finance.service

import com.example.finance.dto.transaction.TransactionRequest
import com.example.finance.dto.transaction.TransactionUpdateRequest
import com.example.finance.entity.Category
import com.example.finance.entity.CategoryType
import com.example.finance.entity.Transaction
import com.example.finance.exception.BadRequestException
import com.example.finance.exception.ForbiddenAccessException
import com.example.finance.exception.ResourceNotFoundException
import com.example.finance.repository.TransactionRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class TransactionServiceTest {

    private val transactionRepository: TransactionRepository = mockk()
    private val categoryService: CategoryService = mockk()

    private lateinit var transactionService: TransactionService

    @BeforeEach
    fun setUp() {
        transactionService = TransactionService(transactionRepository, categoryService)
    }

    @Test
    fun `createTransaction success`() {
        val cat = Category(1L, "Salary", CategoryType.INCOME, null, false)
        val req = TransactionRequest(
            amount = BigDecimal("50000.00"),
            date = LocalDate.now(),
            category = "Salary",
            description = "Jan Pay"
        )

        every { categoryService.findCategoryForUser("Salary", 10L) } returns cat
        every { transactionRepository.save(any()) } answers {
            val t = firstArg<Transaction>()
            t.id = 100L
            t
        }

        val res = transactionService.createTransaction(10L, req)

        assertEquals(100L, res.id)
        assertEquals(BigDecimal("50000.00"), res.amount)
        assertEquals("Salary", res.category)
        assertEquals(CategoryType.INCOME, res.type)
    }

    @Test
    fun `createTransaction negative amount throws BadRequestException`() {
        val req = TransactionRequest(
            amount = BigDecimal("-100.00"),
            date = LocalDate.now(),
            category = "Food"
        )

        assertThrows(BadRequestException::class.java) {
            transactionService.createTransaction(10L, req)
        }
    }

    @Test
    fun `createTransaction future date throws BadRequestException`() {
        val req = TransactionRequest(
            amount = BigDecimal("100.00"),
            date = LocalDate.now().plusDays(1),
            category = "Food"
        )

        assertThrows(BadRequestException::class.java) {
            transactionService.createTransaction(10L, req)
        }
    }

    @Test
    fun `updateTransaction date modification attempt throws BadRequestException`() {
        val cat = Category(1L, "Food", CategoryType.EXPENSE, null, false)
        val existing = Transaction(
            id = 100L,
            userId = 10L,
            amount = BigDecimal("50.00"),
            date = LocalDate.of(2026, 1, 1),
            category = cat
        )

        every { transactionRepository.findById(100L) } returns Optional.of(existing)

        val req = TransactionUpdateRequest(
            amount = BigDecimal("60.00"),
            date = LocalDate.of(2026, 1, 2)
        )

        assertThrows(BadRequestException::class.java) {
            transactionService.updateTransaction(10L, 100L, req)
        }
    }

    @Test
    fun `updateTransaction forbidden user throws ForbiddenAccessException`() {
        val cat = Category(1L, "Food", CategoryType.EXPENSE, null, false)
        val existing = Transaction(
            id = 100L,
            userId = 99L, // different user
            amount = BigDecimal("50.00"),
            date = LocalDate.of(2026, 1, 1),
            category = cat
        )

        every { transactionRepository.findById(100L) } returns Optional.of(existing)

        val req = TransactionUpdateRequest(amount = BigDecimal("60.00"))

        assertThrows(ForbiddenAccessException::class.java) {
            transactionService.updateTransaction(10L, 100L, req)
        }
    }
}
