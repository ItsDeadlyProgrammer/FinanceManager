package com.example.finance.controller

import com.example.finance.dto.auth.MessageResponse
import com.example.finance.dto.transaction.TransactionListResponse
import com.example.finance.dto.transaction.TransactionRequest
import com.example.finance.dto.transaction.TransactionResponse
import com.example.finance.dto.transaction.TransactionUpdateRequest
import com.example.finance.entity.CategoryType
import com.example.finance.security.UserResolver
import com.example.finance.service.TransactionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping(value = ["/api/transactions", "/transactions"])
@Tag(name = "Transactions", description = "Endpoints for managing financial transactions")
class TransactionController(
    private val transactionService: TransactionService,
    private val userResolver: UserResolver
) {

    @PostMapping
    @Operation(summary = "Create a new transaction")
    fun createTransaction(
        authentication: Authentication?,
        @Valid @RequestBody request: TransactionRequest
    ): ResponseEntity<TransactionResponse> {
        val userId = userResolver.resolveUserId(authentication)
        val response = transactionService.createTransaction(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(summary = "Get user transactions with optional filters")
    fun getTransactions(
        authentication: Authentication?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) type: CategoryType?
    ): ResponseEntity<TransactionListResponse> {
        val userId = userResolver.resolveUserId(authentication)
        val response = transactionService.getTransactions(
            userId = userId,
            startDate = startDate,
            endDate = endDate,
            categoryId = categoryId,
            category = category,
            type = type
        )
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing transaction")
    fun updateTransaction(
        authentication: Authentication?,
        @PathVariable id: Long,
        @Valid @RequestBody request: TransactionUpdateRequest
    ): ResponseEntity<TransactionResponse> {
        val userId = userResolver.resolveUserId(authentication)
        val response = transactionService.updateTransaction(userId, id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction by ID")
    fun deleteTransaction(
        authentication: Authentication?,
        @PathVariable id: Long
    ): ResponseEntity<MessageResponse> {
        val userId = userResolver.resolveUserId(authentication)
        transactionService.deleteTransaction(userId, id)
        return ResponseEntity.ok(MessageResponse("Transaction deleted successfully"))
    }
}
