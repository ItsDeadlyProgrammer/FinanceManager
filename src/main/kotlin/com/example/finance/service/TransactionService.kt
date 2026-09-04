package com.example.finance.service

import com.example.finance.dto.transaction.TransactionListResponse
import com.example.finance.dto.transaction.TransactionRequest
import com.example.finance.dto.transaction.TransactionResponse
import com.example.finance.dto.transaction.TransactionUpdateRequest
import com.example.finance.entity.CategoryType
import com.example.finance.entity.Transaction
import com.example.finance.exception.BadRequestException
import com.example.finance.exception.ForbiddenAccessException
import com.example.finance.exception.ResourceNotFoundException
import com.example.finance.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val categoryService: CategoryService
) {

    @Transactional
    fun createTransaction(userId: Long, request: TransactionRequest): TransactionResponse {
        if (request.amount <= BigDecimal.ZERO) {
            throw BadRequestException("Amount must be greater than zero")
        }
        if (request.date.isAfter(LocalDate.now())) {
            throw BadRequestException("Transaction date cannot be a future date")
        }

        val category = categoryService.findCategoryForUser(request.category, userId)

        val transaction = Transaction(
            userId = userId,
            amount = request.amount,
            date = request.date,
            category = category,
            description = request.description?.trim(),
            isDeleted = false
        )
        val saved = transactionRepository.save(transaction)

        return mapToResponse(saved)
    }

    @Transactional(readOnly = true)
    fun getTransactions(
        userId: Long,
        startDate: LocalDate?,
        endDate: LocalDate?,
        categoryId: Long?,
        category: String?,
        type: CategoryType?
    ): TransactionListResponse {
        val list = transactionRepository.findAllFiltered(
            userId = userId,
            startDate = startDate,
            endDate = endDate,
            categoryId = categoryId,
            categoryName = category?.trim(),
            type = type
        )
        return TransactionListResponse(transactions = list.map { mapToResponse(it) })
    }

    @Transactional
    fun updateTransaction(userId: Long, id: Long, request: TransactionUpdateRequest): TransactionResponse {
        val existing = transactionRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Transaction with id $id not found")
        }

        if (existing.userId != userId) {
            throw ForbiddenAccessException("User is not authorized to access this transaction")
        }

        if (existing.isDeleted) {
            throw ResourceNotFoundException("Transaction with id $id not found")
        }

        // The requirement says date field cannot be updated, and should be ignored if provided.
        // So we just don't update it.

        if (request.amount != null) {
            if (request.amount <= BigDecimal.ZERO) {
                throw BadRequestException("Amount must be greater than zero")
            }
            existing.amount = request.amount
        }

        if (request.category != null && request.category.isNotBlank()) {
            val category = categoryService.findCategoryForUser(request.category, userId)
            existing.category = category
        }

        if (request.description != null) {
            existing.description = request.description.trim()
        }

        val updated = transactionRepository.save(existing)
        return mapToResponse(updated)
    }

    @Transactional
    fun deleteTransaction(userId: Long, id: Long) {
        val existing = transactionRepository.findById(id).orElseThrow {
            ResourceNotFoundException("Transaction with id $id not found")
        }

        if (existing.userId != userId) {
            throw ForbiddenAccessException("User is not authorized to access this transaction")
        }

        if (existing.isDeleted) {
            throw ResourceNotFoundException("Transaction with id $id not found")
        }

        existing.isDeleted = true
        transactionRepository.save(existing)
    }

    private fun mapToResponse(t: Transaction): TransactionResponse {
        return TransactionResponse(
            id = t.id!!,
            amount = t.amount,
            date = t.date,
            category = t.category.name,
            description = t.description,
            type = t.category.type
        )
    }
}
