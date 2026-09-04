package com.example.finance.service

import com.example.finance.dto.report.MonthlyReportResponse
import com.example.finance.dto.report.YearlyReportResponse
import com.example.finance.entity.CategoryType
import com.example.finance.exception.BadRequestException
import com.example.finance.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class ReportService(
    private val transactionRepository: TransactionRepository
) {

    @Transactional(readOnly = true)
    fun getMonthlyReport(userId: Long, year: Int, month: Int): MonthlyReportResponse {
        if (month < 1 || month > 12) {
            throw BadRequestException("Invalid month: $month. Month must be between 1 and 12.")
        }
        if (year < 1900 || year > 2100) {
            throw BadRequestException("Invalid year: $year")
        }

        val transactions = transactionRepository.findAllByUserIdAndYearAndMonth(userId, year, month)

        val totalIncomeMap = mutableMapOf<String, BigDecimal>()
        val totalExpensesMap = mutableMapOf<String, BigDecimal>()
        var sumIncome = BigDecimal.ZERO
        var sumExpenses = BigDecimal.ZERO

        for (t in transactions) {
            val catName = t.category.name
            val amount = t.amount

            if (t.category.type == CategoryType.INCOME) {
                totalIncomeMap[catName] = totalIncomeMap.getOrDefault(catName, BigDecimal.ZERO).add(amount)
                sumIncome = sumIncome.add(amount)
            } else if (t.category.type == CategoryType.EXPENSE) {
                totalExpensesMap[catName] = totalExpensesMap.getOrDefault(catName, BigDecimal.ZERO).add(amount)
                sumExpenses = sumExpenses.add(amount)
            }
        }

        val netSavings = sumIncome.subtract(sumExpenses)

        return MonthlyReportResponse(
            month = month,
            year = year,
            totalIncome = totalIncomeMap,
            totalExpenses = totalExpensesMap,
            netSavings = netSavings
        )
    }

    @Transactional(readOnly = true)
    fun getYearlyReport(userId: Long, year: Int): YearlyReportResponse {
        if (year < 1900 || year > 2100) {
            throw BadRequestException("Invalid year: $year")
        }

        val transactions = transactionRepository.findAllByUserIdAndYear(userId, year)

        val totalIncomeMap = mutableMapOf<String, BigDecimal>()
        val totalExpensesMap = mutableMapOf<String, BigDecimal>()
        var sumIncome = BigDecimal.ZERO
        var sumExpenses = BigDecimal.ZERO

        for (t in transactions) {
            val catName = t.category.name
            val amount = t.amount

            if (t.category.type == CategoryType.INCOME) {
                totalIncomeMap[catName] = totalIncomeMap.getOrDefault(catName, BigDecimal.ZERO).add(amount)
                sumIncome = sumIncome.add(amount)
            } else if (t.category.type == CategoryType.EXPENSE) {
                totalExpensesMap[catName] = totalExpensesMap.getOrDefault(catName, BigDecimal.ZERO).add(amount)
                sumExpenses = sumExpenses.add(amount)
            }
        }

        val netSavings = sumIncome.subtract(sumExpenses)

        return YearlyReportResponse(
            year = year,
            totalIncome = totalIncomeMap,
            totalExpenses = totalExpensesMap,
            netSavings = netSavings
        )
    }
}
