package nexters.tuk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
class TukBatchApplication

fun main(args: Array<String>) {
    runApplication<TukBatchApplication>(*args)
}
