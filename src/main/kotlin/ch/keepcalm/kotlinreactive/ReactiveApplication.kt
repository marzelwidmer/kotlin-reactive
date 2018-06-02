package ch.keepcalm.kotlinreactive

import org.reactivestreams.Publisher
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router

@SpringBootApplication
class ReactiveApplication

fun main(args: Array<String>) {

    SpringApplicationBuilder()
            .sources(ReactiveApplication::class.java)
            .initializers(beans {
                bean {
                    WebClient.builder()
                            .baseUrl("http://127.0.0.1:8080/actuator/info")
                            .build()
                }
                bean {
                    router {

                        val client = ref<WebClient>()
                        GET("/profile") {
                            val profiles: Publisher<String> =
                                    client
                                            .get()
                                            .retrieve()
                                            .bodyToFlux<Info>()
                                            .map { it.profile }

                            ServerResponse.ok().body(profiles)
                        }
                    }
                }

                bean {
                    val builder = ref<RouteLocatorBuilder>()
                    builder.routes {
                        route {
//                            host("*.foo.com") and
                                    path("/proxy")
                            uri("http://localhost:8080/actuator/info")
//    service discovery  uri("lb:/my-service/info")
                        }
                    }

                }

            })
            .run(*args)
}

class Info(val profile: String?)