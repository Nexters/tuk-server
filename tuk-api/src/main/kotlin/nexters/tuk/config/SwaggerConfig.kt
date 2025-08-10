package nexters.tuk.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.context.annotation.Configuration


@OpenAPIDefinition(servers = [Server(url = "/", description = "Default Server URL")])
@SecurityScheme(
    name = "Authorization",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
@Configuration
class SwaggerConfig {
    companion object {
        const val SECURITY_SCHEME_NAME = "Authorization"
    }
}