package com.example.finance.service

import com.example.finance.dto.category.CategoryRequest
import com.example.finance.entity.Category
import com.example.finance.entity.CategoryType
import com.example.finance.exception.BadRequestException
import com.example.finance.exception.ResourceAlreadyExistsException
import com.example.finance.exception.ResourceNotFoundException
import com.example.finance.repository.CategoryRepository
import com.example.finance.repository.TransactionRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CategoryServiceTest {

    private val categoryRepository: CategoryRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()

    private lateinit var categoryService: CategoryService

    @BeforeEach
    fun setUp() {
        categoryService = CategoryService(categoryRepository, transactionRepository)
    }

    @Test
    fun `getCategories returns default and user custom categories`() {
        val cat1 = Category(1L, "Salary", CategoryType.INCOME, null, false)
        val cat2 = Category(2L, "SideBiz", CategoryType.INCOME, 10L, true)

        every { categoryRepository.findAllForUser(10L) } returns listOf(cat1, cat2)

        val res = categoryService.getCategories(10L)

        assertEquals(2, res.categories.size)
        assertEquals("Salary", res.categories[0].name)
        assertFalse(res.categories[0].custom)
        assertEquals("SideBiz", res.categories[1].name)
        assertTrue(res.categories[1].custom)
    }

    @Test
    fun `createCustomCategory success`() {
        val req = CategoryRequest("Consulting", CategoryType.INCOME)

        every { categoryRepository.existsByNameAndUserId("Consulting", 10L) } returns false
        every { categoryRepository.existsByNameAndIsCustomFalse("Consulting") } returns false
        every { categoryRepository.save(any()) } answers {
            val c = firstArg<Category>()
            c.id = 5L
            c
        }

        val res = categoryService.createCustomCategory(10L, req)

        assertEquals("Consulting", res.name)
        assertEquals(CategoryType.INCOME, res.type)
        assertTrue(res.custom)
    }

    @Test
    fun `createCustomCategory duplicate name throws ResourceAlreadyExistsException`() {
        val req = CategoryRequest("Food", CategoryType.EXPENSE)

        every { categoryRepository.existsByNameAndUserId("Food", 10L) } returns false
        every { categoryRepository.existsByNameAndIsCustomFalse("Food") } returns true

        assertThrows(ResourceAlreadyExistsException::class.java) {
            categoryService.createCustomCategory(10L, req)
        }
    }

    @Test
    fun `deleteCustomCategory default category throws BadRequestException`() {
        val defaultCat = Category(1L, "Food", CategoryType.EXPENSE, null, false)
        every { categoryRepository.findByNameAndIsCustomFalse("Food") } returns defaultCat

        assertThrows(BadRequestException::class.java) {
            categoryService.deleteCustomCategory(10L, "Food")
        }
    }

    @Test
    fun `deleteCustomCategory referenced category throws ResourceAlreadyExistsException`() {
        val customCat = Category(5L, "MyCat", CategoryType.EXPENSE, 10L, true)
        every { categoryRepository.findByNameAndIsCustomFalse("MyCat") } returns null
        every { categoryRepository.findByNameAndUserId("MyCat", 10L) } returns customCat
        every { transactionRepository.countByCategoryIdAndIsDeletedFalse(5L) } returns 3L

        assertThrows(ResourceAlreadyExistsException::class.java) {
            categoryService.deleteCustomCategory(10L, "MyCat")
        }
    }

    @Test
    fun `deleteCustomCategory non referenced success`() {
        val customCat = Category(5L, "MyCat", CategoryType.EXPENSE, 10L, true)
        every { categoryRepository.findByNameAndIsCustomFalse("MyCat") } returns null
        every { categoryRepository.findByNameAndUserId("MyCat", 10L) } returns customCat
        every { transactionRepository.countByCategoryIdAndIsDeletedFalse(5L) } returns 0L
        every { categoryRepository.delete(customCat) } returns Unit

        categoryService.deleteCustomCategory(10L, "MyCat")

        verify(exactly = 1) { categoryRepository.delete(customCat) }
    }
}
