package dev.ktml.example.spring

import dev.ktml.KtmlDynamicRegistry
import dev.ktml.spring.KtmlViewResolver
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootApplication
open class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Configuration
@EnableWebMvc
open class WebConfig {
    @Bean
    open fun ktmlViewResolver(): ViewResolver {
        return KtmlViewResolver(KtmlDynamicRegistry("applications/example-spring/src/main/ktml"))
    }
}
