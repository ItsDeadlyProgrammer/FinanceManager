package com.example.finance.service

import com.example.finance.dto.goal.GoalRequest
import com.example.finance.dto.goal.GoalUpdateRequest
import com.example.finance.entity.Category
import com.example.finance.entity.CategoryType
import com.example.finance.entity.SavingsGoal
import com.example.finance.entity.Transaction
import com.example.finance.exception.BadRequestException
import com.example.finance.exception.ForbiddenAccessException
import com.example.finance.exception.ResourceNotFoundException
import com.example.finance.repository.SavingsGoalRepository
import com.example.finance.repository.TransactionRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class SavingsGoalServiceTest {

    private val savingsGoalRepository: SavingsGoalRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()

    private lateinit var goalService: SavingsGoalService

    @BeforeEach
    fun setUp() {
        goalService = SavingsGoalService(savingsGoalRepository, transactionRepository)
    }

    @Test
    fun `createGoal success and progress calculation`() {
        val today = LocalDate.now()
        val targetDate = today.plusDays(30)
        val startDate = today.minusDays(10)

        val req = GoalRequest(
            goalName = "Emergency Fund",
            targetAmount = BigDecimal("5000.00"),
            targetDate = targetDate,
            startDate = startDate
        )

        every { savingsGoalRepository.save(any()) } answers {
            val g = firstArg<SavingsGoal>()
            g.id = 1L
            g
        }

        val catInc = Category(1L, "Salary", CategoryType.INCOME, null, false)
        val catExp = Category(2L, "Food", CategoryType.EXPENSE, null, false)

        val t1 = Transaction(101L, 10L, BigDecimal("3000.00"), startDate, catInc)
        val t2 = Transaction(102L, 10L, BigDecimal("1000.00"), startDate.plusDays(1), catExp)

        every { transactionRepository.findAllForGoalProgress(10L, startDate, today) } returns listOf(t1, t2)

        val res = goalService.createGoal(10L, req)

        assertEquals(1L, res.id)
        assertEquals("Emergency Fund", res.goalName)
        assertEquals(BigDecimal("2000.00"), res.currentProgress)
        assertEquals(BigDecimal("3000.00"), res.remainingAmount)
        assertEquals(40.0, res.progressPercentage, 0.01)
    }

    @Test
    fun `getGoals and getGoal by ID`() {
        val goal = SavingsGoal(1L, 10L, "House", BigDecimal("10000.00"), LocalDate.now().plusDays(30), LocalDate.now())
        every { savingsGoalRepository.findByUserId(10L) } returns listOf(goal)
        every { savingsGoalRepository.findById(1L) } returns Optional.of(goal)
        every { transactionRepository.findAllForGoalProgress(10L, any(), any()) } returns emptyList()

        val listRes = goalService.getGoals(10L)
        assertEquals(1, listRes.goals.size)

        val singleRes = goalService.getGoal(10L, 1L)
        assertEquals("House", singleRes.goalName)
    }

    @Test
    fun `getGoal forbidden throws ForbiddenAccessException`() {
        val goal = SavingsGoal(1L, 99L, "House", BigDecimal("10000.00"), LocalDate.now().plusDays(30), LocalDate.now())
        every { savingsGoalRepository.findById(1L) } returns Optional.of(goal)

        assertThrows(ForbiddenAccessException::class.java) {
            goalService.getGoal(10L, 1L)
        }
    }

    @Test
    fun `updateGoal success and deleteGoal success`() {
        val goal = SavingsGoal(1L, 10L, "House", BigDecimal("10000.00"), LocalDate.now().plusDays(30), LocalDate.now())
        every { savingsGoalRepository.findById(1L) } returns Optional.of(goal)
        every { savingsGoalRepository.save(any()) } returns goal
        every { savingsGoalRepository.delete(goal) } returns Unit
        every { transactionRepository.findAllForGoalProgress(10L, any(), any()) } returns emptyList()

        val updateReq = GoalUpdateRequest(targetAmount = BigDecimal("12000.00"), goalName = "New House")
        val updated = goalService.updateGoal(10L, 1L, updateReq)

        assertEquals("New House", updated.goalName)
        assertEquals(BigDecimal("12000.00"), updated.targetAmount)

        goalService.deleteGoal(10L, 1L)
        verify(exactly = 1) { savingsGoalRepository.delete(goal) }
    }
}
