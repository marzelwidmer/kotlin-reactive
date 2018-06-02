package ch.keepcalm.kotlinreactive

import org.reactivestreams.Publisher
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.beans
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono

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
                            path("/proxy")
                            uri("http://localhost:8080/actuator/info")
                            // uri("lb:/my-service/info") // service discovery url
                        }

//                        route {
//                            val rl = ref<RequestRateLimiterGatewayFilterFactory>()
//                            val redisRl = rl.apply(RedisRateLimiter(5, 10))
//                            path("/rl")
//                            filters {
//                                filter(redisRl)
//                            }
//                            uri("http:localhost:8080/actuator/info")
//                        }
                    }

                }

            })
            .run(*args)
}


class Info(val profile: String?)

@Component
class ReactiveHandler(val repo: NamesRepo) {
    fun getName(search: String): Mono<String> = repo.get(search).toMono().map { "Result: $it!" }
    fun addName(text: String): Mono<String> = repo.add(text).toMono().map { "Result: $it!" }
    fun getAllNames(): Flux<String> = repo.getAll().toFlux().map { "Result: $it" }
}

@Repository
class NamesRepo {
    private val entities = mutableListOf<String>()
    fun add(name: String) = entities.add(name)
    fun get(name: String) = entities.find { it == name } ?: "not found!"
    fun getAll() = listOf(entities)
}

@Configuration
class RoutingConfiguration {

    @Bean
    fun routerFunction(handler: ReactiveHandler): RouterFunction<ServerResponse> = router {
        ("/reactive").nest {
            val searchPathName = "search"
            val savePathName = "save"
            GET("/{$searchPathName}") { req ->
                val pathVar = req.pathVariable(searchPathName)
                ok().body(handler.getName(pathVar))
            }
            GET("/") {
                ok().body(handler.getAllNames())
            }
            PUT("/{$savePathName}") { req ->
                val pathVar = req.pathVariable(savePathName)
                ok().body(handler.addName(pathVar))
            }
        }
    }
}