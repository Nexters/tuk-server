package nexters.tuk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TukApplication

fun main(args: Array<String>) {
	runApplication<TukApplication>(*args)
}
