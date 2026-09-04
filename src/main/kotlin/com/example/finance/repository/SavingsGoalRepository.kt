package com.example.finance.repository

import com.example.finance.entity.SavingsGoal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SavingsGoalRepository : JpaRepository<SavingsGoal, Long> {
    fun findByUserId(userId: Long): List<SavingsGoal>
    fun findByIdAndUserId(id: Long, userId: Long): SavingsGoal?
}
