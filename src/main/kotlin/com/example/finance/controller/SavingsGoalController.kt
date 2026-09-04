package com.example.finance.controller

import com.example.finance.dto.auth.MessageResponse
import com.example.finance.dto.goal.GoalListResponse
import com.example.finance.dto.goal.GoalRequest
import com.example.finance.dto.goal.GoalResponse
import com.example.finance.dto.goal.GoalUpdateRequest
import com.example.finance.security.UserResolver
import com.example.finance.service.SavingsGoalService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/goals", "/goals"])
@Tag(name = "Savings Goals", description = "Endpoints for managing savings goals and tracking progress")
class SavingsGoalController(
    private val savingsGoalService: SavingsGoalService,
    private val userResolver: UserResolver
) {

    @PostMapping
    @Operation(summary = "Create a new savings goal")
    fun createGoal(
        authentication: Authentication?,
        @Valid @RequestBody request: GoalRequest
    ): ResponseEntity<GoalResponse> {
        val userId = userResolver.resolveUserId(authentication)
        val response = savingsGoalService.createGoal(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get all savings goals for authenticated user")
    fun getGoals(authentication: Authentication?): ResponseEntity<GoalListResponse> {
        val userId = userResolver.resolveUserId(authentication)
        val response = savingsGoalService.getGoals(userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific savings goal by ID")
    fun getGoal(
        authentication: Authentication?,
        @PathVariable id: Long
    ): ResponseEntity<GoalResponse> {
        val userId = userResolver.resolveUserId(authentication)
        val response = savingsGoalService.getGoal(userId, id)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a savings goal")
    fun updateGoal(
        authentication: Authentication?,
        @PathVariable id: Long,
        @Valid @RequestBody request: GoalUpdateRequest
    ): ResponseEntity<GoalResponse> {
        val userId = userResolver.resolveUserId(authentication)
        val response = savingsGoalService.updateGoal(userId, id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a savings goal by ID")
    fun deleteGoal(
        authentication: Authentication?,
        @PathVariable id: Long
    ): ResponseEntity<MessageResponse> {
        val userId = userResolver.resolveUserId(authentication)
        savingsGoalService.deleteGoal(userId, id)
        return ResponseEntity.ok(MessageResponse("Goal deleted successfully"))
    }
}
