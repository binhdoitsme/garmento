package org.garmento.tryon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.jms.annotation.EnableJms

@EnableJms
@SpringBootApplication
class TryOnApplication

fun main(args: Array<String>) {
    runApplication<TryOnApplication>(*args)
}
