package com.example.finance.dto.transaction

import com.example.finance.entity.CategoryType
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import java.math.BigDecimal
import java.time.LocalDate

data class TransactionRequest(
    @field:NotNull(message = "Amount is required")
    @field:DecimalMin(value = "0.01", message = "Amount must be positive")
    val amount: BigDecimal = BigDecimal.ZERO,

    @field:NotNull(message = "Date is required")
    @field:PastOrPresent(message = "Transaction date cannot be a future date")
    val date: LocalDate = LocalDate.now(),

    @field:NotBlank(message = "Category is required")
    val category: String = "",

    val description: String? = null
)

data class TransactionUpdateRequest(
    @field:DecimalMin(value = "0.01", message = "Amount must be positive")
    val amount: BigDecimal? = null,

    val category: String? = null,
    val description: String? = null,
    val date: LocalDate? = null
)

data class TransactionResponse(
    val id: Long = 0,
    val amount: BigDecimal = BigDecimal.ZERO,
    val date: LocalDate = LocalDate.now(),
    val category: String = "",
    val description: String? = null,
    val type: CategoryType = CategoryType.EXPENSE
)

data class TransactionListResponse(
    val transactions: List<TransactionResponse> = emptyList()
)
