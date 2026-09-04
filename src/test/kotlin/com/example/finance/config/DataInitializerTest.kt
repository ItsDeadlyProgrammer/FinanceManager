package com.example.finance.config

import com.example.finance.entity.Category
import com.example.finance.repository.CategoryRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DataInitializerTest {

    private val categoryRepository: CategoryRepository = mockk()
    private lateinit var dataInitializer: DataInitializer

    @BeforeEach
    fun setUp() {
        dataInitializer = DataInitializer(categoryRepository)
    }

    @Test
    fun `initDefaultCategories saves missing default categories`() {
        every { categoryRepository.existsByNameAndIsCustomFalse(any()) } returns false
        every { categoryRepository.save(any()) } answers { firstArg<Category>() }

        dataInitializer.initDefaultCategories()

        verify(atLeast = 7) { categoryRepository.save(any()) }
    }

    @Test
    fun `initDefaultCategories skips existing categories`() {
        every { categoryRepository.existsByNameAndIsCustomFalse(any()) } returns true

        dataInitializer.initDefaultCategories()

        verify(exactly = 0) { categoryRepository.save(any()) }
    }
}
