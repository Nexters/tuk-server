package nexters.tuk.testcontainers

import com.redis.testcontainers.RedisContainer
import org.springframework.context.annotation.Configuration

@Configuration
class RedisTestContainersConfig {
    companion object {
        private val redisContainer: RedisContainer = RedisContainer("redis:latest")
            .apply {
                start()
            }

        init {
            System.setProperty("spring.data.redis.database", "0")
            System.setProperty("spring.data.redis.host", redisContainer.host)
            System.setProperty("spring.data.redis.port", redisContainer.firstMappedPort.toString())
        }
    }
}
