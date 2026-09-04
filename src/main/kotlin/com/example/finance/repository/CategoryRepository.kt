package com.example.finance.repository

import com.example.finance.entity.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.isCustom = false OR c.userId = :userId ORDER BY c.id ASC")
    fun findAllForUser(userId: Long): List<Category>

    @Query("SELECT c FROM Category c WHERE c.name = :name AND (c.isCustom = false OR c.userId = :userId)")
    fun findByNameForUser(name: String, userId: Long): Category?

    fun findByNameAndUserId(name: String, userId: Long): Category?
    fun findByNameAndIsCustomFalse(name: String): Category?

    fun existsByNameAndUserId(name: String, userId: Long): Boolean
    fun existsByNameAndIsCustomFalse(name: String): Boolean
}
