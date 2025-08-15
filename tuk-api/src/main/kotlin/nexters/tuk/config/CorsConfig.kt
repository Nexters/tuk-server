package nexters.tuk.config

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {

    @Bean
    fun corsFilterRegistration(): FilterRegistrationBean<CorsFilter> {
        val config = CorsConfiguration().apply {
            allowedOriginPatterns = listOf(
                "http://localhost:3000",
                "https://tuk.kr",
                "https://www.tuk.kr"
            )
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            allowedHeaders = listOf("Content-Type", "Authorization", "X-Requested-With", "Accept")
            allowCredentials = true
            maxAge = 3600
        }

        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }

        return FilterRegistrationBean(CorsFilter(source)).apply {
            order = Ordered.HIGHEST_PRECEDENCE
        }
    }
}