package io.eganjs.evil.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EvilApplication

fun main(args: Array<String>) {
    runApplication<EvilApplication>(*args)
}
