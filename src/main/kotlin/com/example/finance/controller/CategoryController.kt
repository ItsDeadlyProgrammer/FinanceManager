package com.example.finance.controller

import com.example.finance.dto.auth.MessageResponse
import com.example.finance.dto.category.CategoryListResponse
import com.example.finance.dto.category.CategoryRequest
import com.example.finance.dto.category.CategoryResponse
import com.example.finance.security.UserResolver
import com.example.finance.service.CategoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/categories", "/categories"])
@Tag(name = "Categories", description = "Endpoints for managing transaction categories")
class CategoryController(
    private val categoryService: CategoryService,
    private val userResolver: UserResolver
) {

    @GetMapping
    @Operation(summary = "Get all categories available to the user")
    fun getCategories(authentication: Authentication?): ResponseEntity<CategoryListResponse> {
        val userId = userResolver.resolveUserId(authentication)
        val response = categoryService.getCategories(userId)
        return ResponseEntity.ok(response)
    }

    @PostMapping
    @Operation(summary = "Create a new custom category")
    fun createCategory(
        authentication: Authentication?,
        @Valid @RequestBody request: CategoryRequest
    ): ResponseEntity<CategoryResponse> {
        val userId = userResolver.resolveUserId(authentication)
        val response = categoryService.createCustomCategory(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/{name}")
    @Operation(summary = "Delete a custom category by name")
    fun deleteCategory(
        authentication: Authentication?,
        @PathVariable name: String
    ): ResponseEntity<MessageResponse> {
        val userId = userResolver.resolveUserId(authentication)
        categoryService.deleteCustomCategory(userId, name)
        return ResponseEntity.ok(MessageResponse("Category deleted successfully"))
    }
}
