package dev.ktml.spring

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(KtmlViewResolver::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
open class KtmlAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    open fun ktmlViewResolver() = KtmlViewResolver()
}
