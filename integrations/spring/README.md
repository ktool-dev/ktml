# KTML Spring Boot Integration

Provides a ViewResolver for rendering KTML templates in Spring Boot applications.

## Installation

Register the view resolver as a Spring bean:

```kotlin
import dev.ktml.spring.KtmlViewResolver
import dev.ktml.KtmlRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KtmlConfig {
    @Bean
    fun ktmlViewResolver(): KtmlViewResolver {
        val registry = KtmlRegistry() // Your template registry
        return KtmlViewResolver(registry)
    }
}
```

## Usage

Use KTML templates in your Spring MVC controllers by returning the template path:

```kotlin
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController {

    @GetMapping("/")
    fun home(model: Model): String {
        model.addAttribute("title", "Home Page")
        return "/index" // KTML template path
    }

    @GetMapping("/user/{id}")
    fun user(@PathVariable id: String, model: Model): String {
        model.addAttribute("userId", id)
        return "/user"
    }
}
```

## Features

The view resolver automatically:

- Sets the `Content-Type` header to `text/html;charset=UTF-8`
- Passes Spring model attributes to the template
- Extracts and passes HTTP query parameters to the template context
- Extracts and passes URL path parameters to the template context
- Renders templates in blocking mode

## Template Context

Templates receive a `Context` object with:

- **`model`**: Spring model attributes as `Map<String, Any?>`
- **`queryParams`**: HTTP query parameters as `Map<String, List<String>>`
- **`pathParams`**: URL path parameters as `Map<String, List<String>>`

## Example

```kotlin
@Configuration
class KtmlConfig {
    @Bean
    fun ktmlViewResolver(): KtmlViewResolver {
        return KtmlViewResolver(myTemplateRegistry)
    }
}

@Controller
class ProductController(private val productService: ProductService) {

    @GetMapping("/products")
    fun listProducts(model: Model): String {
        val products = productService.findAll()
        model.addAttribute("products", products)
        model.addAttribute("count", products.size)
        return "/products/list"
    }

    @GetMapping("/products/{id}")
    fun showProduct(@PathVariable id: Long, model: Model): String {
        val product = productService.findById(id)
        model.addAttribute("product", product)
        return "/products/detail"
    }

    @GetMapping("/search")
    fun search(@RequestParam q: String, model: Model): String {
        val results = productService.search(q)
        model.addAttribute("query", q)
        model.addAttribute("results", results)
        return "/search"
    }
}
```

## Integration with Spring Boot

The `KtmlViewResolver` implements Spring's `ViewResolver` interface and integrates seamlessly with Spring MVC's view resolution chain. You can use it alongside other view resolvers (e.g., Thymeleaf, JSP) by configuring the appropriate order.
