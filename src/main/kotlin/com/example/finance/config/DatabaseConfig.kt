package com.example.finance.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DatabaseConfig(
    @Value("\${DATABASE_URL:jdbc:h2:mem:financedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL}")
    private val databaseUrl: String,

    @Value("\${DB_USERNAME:sa}")
    private val username: String,

    @Value("\${DB_PASSWORD:}")
    private val password: String
) {

    @Bean
    fun dataSource(): DataSource {
        val jdbcUrl = if (databaseUrl.startsWith("jdbc:")) {
            databaseUrl
        } else {
            "jdbc:$databaseUrl"
        }

        return HikariDataSource().apply {
            this.jdbcUrl = jdbcUrl
            this.username = username
            this.password = password
        }
    }
}