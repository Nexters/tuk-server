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
            System.setProperty("spring.data.redis.master.host", redisContainer.host)
            System.setProperty("spring.data.redis.master.port", redisContainer.firstMappedPort.toString())
            System.setProperty("spring.data.redis.replicas[0].host", redisContainer.host)
            System.setProperty("spring.data.redis.replicas[0].port", redisContainer.firstMappedPort.toString())
        }
    }
}
