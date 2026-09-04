package com.example.finance.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Personal Finance Manager API")
                    .version("1.0.0")
                    .description("Production-grade Personal Finance Manager API for Syfe Take-Home Backend Assignment. Features session authentication, strict user multi-tenancy data isolation, transaction management, categories, savings goals, and financial reporting.")
                    .contact(
                        Contact()
                            .name("Syfe Backend Candidate")
                            .email("candidate@syfe.com")
                    )
            )
    }
}
