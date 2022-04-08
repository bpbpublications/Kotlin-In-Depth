package ch17.randomGen

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RandomGenerator

fun main(args: Array<String>) {
	runApplication<RandomGenerator>(*args)
}
