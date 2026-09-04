package com.example.finance.dto.error

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ErrorResponse(
    val status: Int,
    val message: String,
    val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
)
