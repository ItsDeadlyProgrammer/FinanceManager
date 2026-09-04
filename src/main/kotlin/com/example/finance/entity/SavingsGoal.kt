package com.example.finance.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "savings_goals")
class SavingsGoal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var userId: Long = 0,

    @Column(nullable = false)
    var goalName: String = "",

    @Column(nullable = false, precision = 19, scale = 2)
    var targetAmount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var targetDate: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    var startDate: LocalDate = LocalDate.now(),

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
