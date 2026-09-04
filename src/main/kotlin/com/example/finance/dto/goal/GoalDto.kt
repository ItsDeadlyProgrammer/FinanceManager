package com.example.finance.dto.goal

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate

data class GoalRequest(
    @field:NotBlank(message = "Goal name cannot be blank")
    val goalName: String = "",

    @field:NotNull(message = "Target amount is required")
    @field:DecimalMin(value = "0.01", message = "Target amount must be positive")
    val targetAmount: BigDecimal = BigDecimal.ZERO,

    @field:NotNull(message = "Target date is required")
    @field:Future(message = "Target date must be in the future")
    val targetDate: LocalDate = LocalDate.now().plusDays(1),

    val startDate: LocalDate? = null
)

data class GoalUpdateRequest(
    @field:DecimalMin(value = "0.01", message = "Target amount must be positive")
    val targetAmount: BigDecimal? = null,

    @field:Future(message = "Target date must be in the future")
    val targetDate: LocalDate? = null,

    val goalName: String? = null
)

data class GoalResponse(
    val id: Long = 0,
    val goalName: String = "",
    val targetAmount: BigDecimal = BigDecimal.ZERO,
    val targetDate: LocalDate = LocalDate.now(),
    val startDate: LocalDate = LocalDate.now(),
    val currentProgress: BigDecimal = BigDecimal.ZERO,
    val progressPercentage: Double = 0.0,
    val remainingAmount: BigDecimal = BigDecimal.ZERO
)

data class GoalListResponse(
    val goals: List<GoalResponse> = emptyList()
)
