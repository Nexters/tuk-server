package nexters.tuk.infrastructure.http

import nexters.tuk.domain.push.PushApiClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import java.time.Duration

@Configuration
class RestClientConfig {
    @Bean
    fun restClient(): RestClient {
        return RestClient.builder()
            .baseUrl("http://tuk-api:8080")
            .requestFactory(clientHttpRequestFactory())
            .build()
    }

    @Bean
    fun clientHttpRequestFactory(): ClientHttpRequestFactory {
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(Duration.ofSeconds(1))
        factory.setReadTimeout(Duration.ofSeconds(3))
        return factory
    }

    @Bean
    fun pushApiClient(restClient: RestClient): PushApiClient {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
            .build()
            .createClient(PushApiClient::class.java)
    }
}