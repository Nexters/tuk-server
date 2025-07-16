package nexters.tuk.testcontainers

import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.stereotype.Component

@Component
class RedisCleanUp(
    private val redisConnectionFactory: RedisConnectionFactory,
) {
    fun flushAll() {
        redisConnectionFactory.connection.use { it.serverCommands().flushAll() }
    }
}
