package org.garmento.tryon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TryOnApplication

fun main(args: Array<String>) {
	runApplication<TryOnApplication>(*args)
}
