package com.example.finance.service

import com.example.finance.dto.category.CategoryListResponse
import com.example.finance.dto.category.CategoryRequest
import com.example.finance.dto.category.CategoryResponse
import com.example.finance.entity.Category
import com.example.finance.exception.BadRequestException
import com.example.finance.exception.ResourceAlreadyExistsException
import com.example.finance.exception.ResourceNotFoundException
import com.example.finance.repository.CategoryRepository
import com.example.finance.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) {

    @Transactional(readOnly = true)
    fun getCategories(userId: Long): CategoryListResponse {
        val categories = categoryRepository.findAllForUser(userId)
        val dtos = categories.map {
            CategoryResponse(
                name = it.name,
                type = it.type,
                custom = it.isCustom
            )
        }
        return CategoryListResponse(categories = dtos)
    }

    @Transactional
    fun createCustomCategory(userId: Long, request: CategoryRequest): CategoryResponse {
        val nameTrimmed = request.name.trim()
        if (nameTrimmed.isBlank()) {
            throw BadRequestException("Category name cannot be blank")
        }

        // Check duplicate name for this user or default category
        if (categoryRepository.existsByNameAndUserId(nameTrimmed, userId) ||
            categoryRepository.existsByNameAndIsCustomFalse(nameTrimmed)) {
            throw ResourceAlreadyExistsException("Category with name '$nameTrimmed' already exists")
        }

        val category = Category(
            name = nameTrimmed,
            type = request.type,
            userId = userId,
            isCustom = true
        )
        val saved = categoryRepository.save(category)

        return CategoryResponse(
            name = saved.name,
            type = saved.type,
            custom = saved.isCustom
        )
    }

    @Transactional
    fun deleteCustomCategory(userId: Long, categoryName: String) {
        val nameTrimmed = categoryName.trim()

        val defaultCategory = categoryRepository.findByNameAndIsCustomFalse(nameTrimmed)
        if (defaultCategory != null) {
            throw BadRequestException("Default categories cannot be deleted")
        }

        val customCategory = categoryRepository.findByNameAndUserId(nameTrimmed, userId)
            ?: throw ResourceNotFoundException("Custom category '$nameTrimmed' not found")

        val refCount = transactionRepository.countByCategoryIdAndIsDeletedFalse(customCategory.id!!)
        if (refCount > 0) {
            throw ResourceAlreadyExistsException("Category '$nameTrimmed' is referenced by existing transactions and cannot be deleted")
        }

        categoryRepository.delete(customCategory)
    }

    @Transactional(readOnly = true)
    fun findCategoryForUser(categoryName: String, userId: Long): Category {
        val nameTrimmed = categoryName.trim()
        return categoryRepository.findByNameForUser(nameTrimmed, userId)
            ?: throw BadRequestException("Category '$categoryName' does not exist")
    }
}
