package com.example.finance.controller

import com.example.finance.dto.report.MonthlyReportResponse
import com.example.finance.dto.report.YearlyReportResponse
import com.example.finance.security.UserResolver
import com.example.finance.service.ReportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/reports", "/reports"])
@Tag(name = "Reports", description = "Endpoints for generating monthly and yearly financial reports")
class ReportController(
    private val reportService: ReportService,
    private val userResolver: UserResolver
) {

    @GetMapping("/monthly/{year}/{month}")
    @Operation(summary = "Get monthly financial breakdown report")
    fun getMonthlyReport(
        authentication: Authentication?,
        @PathVariable year: Int,
        @PathVariable month: Int
    ): ResponseEntity<MonthlyReportResponse> {
        val userId = userResolver.resolveUserId(authentication)
        val response = reportService.getMonthlyReport(userId, year, month)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/yearly/{year}")
    @Operation(summary = "Get yearly financial breakdown report")
    fun getYearlyReport(
        authentication: Authentication?,
        @PathVariable year: Int
    ): ResponseEntity<YearlyReportResponse> {
        val userId = userResolver.resolveUserId(authentication)
        val response = reportService.getYearlyReport(userId, year)
        return ResponseEntity.ok(response)
    }
}
