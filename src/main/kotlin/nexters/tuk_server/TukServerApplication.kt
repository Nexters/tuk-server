package nexters.tuk_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TukServerApplication

fun main(args: Array<String>) {
	runApplication<TukServerApplication>(*args)
}
