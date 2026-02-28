package dev.ktml.spring

import dev.ktml.DEFAULT_PACKAGE
import dev.ktml.KtmlEngine
import dev.ktml.KtmlRegistry
import dev.ktml.findKtmlRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration(after = [WebMvcAutoConfiguration::class])
@ConditionalOnClass(KtmlViewResolver::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
open class KtmlAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    open fun ktmlRegistry(@Value($$"${ktml.templatePackage:null}") templatePackage: String?) =
        findKtmlRegistry(templatePackage ?: DEFAULT_PACKAGE)

    @Bean
    @ConditionalOnMissingBean
    open fun ktmlEngin(ktmlRegistry: KtmlRegistry) = KtmlEngine(ktmlRegistry)

    @Bean
    @ConditionalOnMissingBean
    open fun ktmlViewResolver(ktmlRegistry: KtmlRegistry, ktmlEngine: KtmlEngine) =
        KtmlViewResolver(ktmlRegistry, ktmlEngine)
}
