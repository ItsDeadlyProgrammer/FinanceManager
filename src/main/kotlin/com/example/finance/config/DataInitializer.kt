package com.example.finance.config

import com.example.finance.entity.Category
import com.example.finance.entity.CategoryType
import com.example.finance.repository.CategoryRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DataInitializer(
    private val categoryRepository: CategoryRepository
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        initDefaultCategories()
    }

    fun initDefaultCategories() {
        val defaultIncome = listOf("Salary")
        val defaultExpenses = listOf(
            "Food",
            "Rent",
            "Transportation",
            "Entertainment",
            "Healthcare",
            "Utilities"
        )

        for (name in defaultIncome) {
            if (!categoryRepository.existsByNameAndIsCustomFalse(name)) {
                categoryRepository.save(
                    Category(
                        name = name,
                        type = CategoryType.INCOME,
                        userId = null,
                        isCustom = false
                    )
                )
            }
        }

        for (name in defaultExpenses) {
            if (!categoryRepository.existsByNameAndIsCustomFalse(name)) {
                categoryRepository.save(
                    Category(
                        name = name,
                        type = CategoryType.EXPENSE,
                        userId = null,
                        isCustom = false
                    )
                )
            }
        }
    }
}
