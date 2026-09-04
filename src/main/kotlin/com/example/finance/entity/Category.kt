package com.example.finance.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "categories",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_user_category_name", columnNames = ["name", "user_id"])
    ]
)
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    var name: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: CategoryType = CategoryType.EXPENSE,

    @Column(name = "user_id", nullable = true)
    var userId: Long? = null,

    @Column(nullable = false)
    var isCustom: Boolean = false,

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
