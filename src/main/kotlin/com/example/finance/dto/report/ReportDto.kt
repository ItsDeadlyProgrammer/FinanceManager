package com.example.finance.dto.report

import java.math.BigDecimal

data class MonthlyReportResponse(
    val month: Int = 1,
    val year: Int = 2026,
    val totalIncome: Map<String, BigDecimal> = emptyMap(),
    val totalExpenses: Map<String, BigDecimal> = emptyMap(),
    val netSavings: BigDecimal = BigDecimal.ZERO
)

data class YearlyReportResponse(
    val year: Int = 2026,
    val totalIncome: Map<String, BigDecimal> = emptyMap(),
    val totalExpenses: Map<String, BigDecimal> = emptyMap(),
    val netSavings: BigDecimal = BigDecimal.ZERO
)
