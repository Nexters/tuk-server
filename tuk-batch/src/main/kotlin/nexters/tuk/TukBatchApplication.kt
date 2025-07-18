package nexters.tuk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TukBatchApplication

fun main(args: Array<String>) {
    runApplication<TukBatchApplication>(*args)
}
