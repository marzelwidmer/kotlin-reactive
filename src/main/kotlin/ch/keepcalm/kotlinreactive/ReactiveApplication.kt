package ch.keepcalm.kotlinreactive

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.support.beans

@SpringBootApplication
class ReactiveApplication

fun main(args: Array<String>) {

    SpringApplicationBuilder()
            .sources(ReactiveApplication::class.java)
            .initializers(beans {

            })
            .run(*args)
}
