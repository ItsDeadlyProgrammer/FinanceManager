package com.example.finance.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class PathNormalizationFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request is HttpServletRequest) {
            val uri = request.requestURI
            if (uri != null && (uri.contains("//") || (uri.length > 1 && uri.endsWith("/")))) {
                var cleanedUri = uri.replace(Regex("/+"), "/")
                if (cleanedUri.length > 1 && cleanedUri.endsWith("/")) {
                    cleanedUri = cleanedUri.substring(0, cleanedUri.length - 1)
                }
                
                val wrappedRequest = object : HttpServletRequestWrapper(request) {
                    override fun getRequestURI(): String = cleanedUri
                    override fun getRequestURL(): StringBuffer {
                        val url = super.getRequestURL().toString()
                        return StringBuffer(url.replace(Regex("([^:])//+"), "$1/"))
                    }
                    override fun getServletPath(): String {
                        val path = super.getServletPath()
                        return if (path.contains("//")) path.replace(Regex("/+"), "/") else path
                    }
                }
                chain.doFilter(wrappedRequest, response)
                return
            }
        }
        chain.doFilter(request, response)
    }
}
