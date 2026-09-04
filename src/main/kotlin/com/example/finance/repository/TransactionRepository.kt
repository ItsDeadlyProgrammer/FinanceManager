package com.example.finance.repository

import com.example.finance.entity.CategoryType
import com.example.finance.entity.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface TransactionRepository : JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    fun findByIdAndUserIdAndIsDeletedFalse(id: Long, userId: Long): Transaction?
    fun findByIdAndUserId(id: Long, userId: Long): Transaction?

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.category.id = :categoryId AND t.isDeleted = false")
    fun countByCategoryIdAndIsDeletedFalse(categoryId: Long): Long

    @Query("SELECT COUNT(t) FROM Transaction t WHERE LOWER(CAST(t.category.name AS string)) = LOWER(CAST(:categoryName AS string)) AND t.isDeleted = false")
    fun countByCategoryNameAndIsDeletedFalse(categoryName: String): Long

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.userId = :userId
          AND t.isDeleted = false
          AND (CAST(:startDate AS string) IS NULL OR t.date >= :startDate)
          AND (CAST(:endDate AS string) IS NULL OR t.date <= :endDate)
          AND (CAST(:categoryId AS string) IS NULL OR t.category.id = :categoryId)
          AND (CAST(:categoryName AS string) IS NULL OR LOWER(CAST(t.category.name AS string)) = LOWER(CAST(:categoryName AS string)))
          AND (CAST(:type AS string) IS NULL OR t.category.type = :type)
        ORDER BY t.date DESC, t.id DESC
    """)
    fun findAllFiltered(
        userId: Long,
        startDate: LocalDate?,
        endDate: LocalDate?,
        categoryId: Long?,
        categoryName: String?,
        type: CategoryType?
    ): List<Transaction>

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.userId = :userId
          AND t.isDeleted = false
          AND t.date >= :startDate
          AND t.date <= :today
    """)
    fun findAllForGoalProgress(userId: Long, startDate: LocalDate, today: LocalDate): List<Transaction>

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.userId = :userId
          AND t.isDeleted = false
          AND t.date >= :startDate
          AND t.date <= :endDate
    """)
    fun findAllByUserIdAndDateBetween(userId: Long, startDate: LocalDate, endDate: LocalDate): List<Transaction>
}
