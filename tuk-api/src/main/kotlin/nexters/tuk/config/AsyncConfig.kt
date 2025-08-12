package nexters.tuk.config

import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.lang.reflect.Method
import java.util.concurrent.Executor


@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {

    companion object {
        private const val ASYNC_TASK_EXECUTOR = "asyncTaskExecutor"
        private val logger = LoggerFactory.getLogger(AsyncConfig::class.java)
    }

    @Bean(name = [ASYNC_TASK_EXECUTOR], destroyMethod = "shutdown")
    fun asyncTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 3
        executor.maxPoolSize = 3
        executor.threadNamePrefix = "asyncTask-"
        executor.queueCapacity = 100
        executor.setAwaitTerminationSeconds(20)
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.initialize()
        executor.threadPoolExecutor.prestartAllCoreThreads()
        return executor
    }

    override fun getAsyncExecutor(): Executor {
        return asyncTaskExecutor()
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return AsyncExceptionHandler()
    }

    class AsyncExceptionHandler : AsyncUncaughtExceptionHandler {
        override fun handleUncaughtException(ex: Throwable, method: Method, vararg params: Any?) {
            logger.error(
                "AsyncUncaught Exception: ${ex.stackTraceToString()}",
            )
        }
    }
}