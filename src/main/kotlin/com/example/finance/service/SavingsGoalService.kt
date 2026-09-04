package com.example.finance.service

import com.example.finance.dto.goal.GoalListResponse
import com.example.finance.dto.goal.GoalRequest
import com.example.finance.dto.goal.GoalResponse
import com.example.finance.dto.goal.GoalUpdateRequest
import com.example.finance.entity.CategoryType
import com.example.finance.entity.SavingsGoal
import com.example.finance.exception.BadRequestException
import com.example.finance.exception.ForbiddenAccessException
import com.example.finance.exception.ResourceNotFoundException
import com.example.finance.repository.SavingsGoalRepository
import com.example.finance.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Service
class SavingsGoalService(
    private val savingsGoalRepository: SavingsGoalRepository,
    private val transactionRepository: TransactionRepository
) {

    @Transactional
    fun createGoal(userId: Long, request: GoalRequest): GoalResponse {
        val today = LocalDate.now()
        val startDate = request.startDate ?: today

        if (request.targetAmount <= BigDecimal.ZERO) {
            throw BadRequestException("Target amount must be positive")
        }
        if (!request.targetDate.isAfter(today)) {
            throw BadRequestException("Target date must be in the future")
        }
        if (startDate.isAfter(request.targetDate)) {
            throw BadRequestException("Start date cannot be after target date")
        }

        val goal = SavingsGoal(
            userId = userId,
            goalName = request.goalName.trim(),
            targetAmount = request.targetAmount,
            targetDate = request.targetDate,
            startDate = startDate
        )
        val saved = savingsGoalRepository.save(goal)
        return calculateProgress(saved)
    }

    @Transactional(readOnly = true)
    fun getGoals(userId: Long): GoalListResponse {
        val goals = savingsGoalRepository.findByUserId(userId)
        return GoalListResponse(goals = goals.map { calculateProgress(it) })
    }

    @Transactional(readOnly = true)
    fun getGoal(userId: Long, id: Long): GoalResponse {
        val goal = savingsGoalRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Goal with id $id not found")
        }

        if (goal.userId != userId) {
            throw ForbiddenAccessException("User is not authorized to access this goal")
        }

        return calculateProgress(goal)
    }

    @Transactional
    fun updateGoal(userId: Long, id: Long, request: GoalUpdateRequest): GoalResponse {
        val goal = savingsGoalRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Goal with id $id not found")
        }

        if (goal.userId != userId) {
            throw ForbiddenAccessException("User is not authorized to access this goal")
        }

        val today = LocalDate.now()

        if (request.targetAmount != null) {
            if (request.targetAmount <= BigDecimal.ZERO) {
                throw BadRequestException("Target amount must be positive")
            }
            goal.targetAmount = request.targetAmount
        }

        if (request.targetDate != null) {
            if (!request.targetDate.isAfter(today)) {
                throw BadRequestException("Target date must be in the future")
            }
            if (goal.startDate.isAfter(request.targetDate)) {
                throw BadRequestException("Start date cannot be after target date")
            }
            goal.targetDate = request.targetDate
        }

        if (request.goalName != null && request.goalName.isNotBlank()) {
            goal.goalName = request.goalName.trim()
        }

        val updated = savingsGoalRepository.save(goal)
        return calculateProgress(updated)
    }

    @Transactional
    fun deleteGoal(userId: Long, id: Long) {
        val goal = savingsGoalRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Goal with id $id not found")
        }

        if (goal.userId != userId) {
            throw ForbiddenAccessException("User is not authorized to access this goal")
        }

        savingsGoalRepository.delete(goal)
    }

    private fun calculateProgress(goal: SavingsGoal): GoalResponse {
        val today = LocalDate.now()
        val transactions = transactionRepository.findAllForGoalProgress(
            userId = goal.userId,
            startDate = goal.startDate,
            today = today
        )

        var totalIncome = BigDecimal.ZERO
        var totalExpenses = BigDecimal.ZERO

        for (t in transactions) {
            if (t.category.type == CategoryType.INCOME) {
                totalIncome = totalIncome.add(t.amount)
            } else if (t.category.type == CategoryType.EXPENSE) {
                totalExpenses = totalExpenses.add(t.amount)
            }
        }

        val currentProgress = totalIncome.subtract(totalExpenses)

        val progressPercentage = if (goal.targetAmount > BigDecimal.ZERO) {
            currentProgress.divide(goal.targetAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .toDouble()
        } else {
            0.0
        }

        val remainingAmount = goal.targetAmount.subtract(currentProgress).max(BigDecimal.ZERO)

        return GoalResponse(
            id = goal.id!!,
            goalName = goal.goalName,
            targetAmount = goal.targetAmount,
            targetDate = goal.targetDate,
            startDate = goal.startDate,
            currentProgress = currentProgress,
            progressPercentage = progressPercentage,
            remainingAmount = remainingAmount
        )
    }
}
