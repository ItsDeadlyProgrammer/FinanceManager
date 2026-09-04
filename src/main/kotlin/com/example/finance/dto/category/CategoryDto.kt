package com.example.finance.dto.category

import com.example.finance.entity.CategoryType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CategoryRequest(
    @field:NotBlank(message = "Category name cannot be blank")
    val name: String = "",

    @field:NotNull(message = "Category type is required")
    val type: CategoryType = CategoryType.EXPENSE
)

data class CategoryResponse(
    val name: String = "",
    val type: CategoryType = CategoryType.EXPENSE,
    val custom: Boolean = false
)

data class CategoryListResponse(
    val categories: List<CategoryResponse> = emptyList()
)
