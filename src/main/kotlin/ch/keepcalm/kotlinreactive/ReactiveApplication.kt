package ch.keepcalm.kotlinreactive

import org.reactivestreams.Publisher
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
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

x
class Info(val profile: String?)