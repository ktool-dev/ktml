package dev.ktml.example.spring.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class HomeController {

    @GetMapping("/")
    fun index(model: Model): String {
        model.addAttribute("title", "Welcome to KTML with Spring MVC")
        return "index"
    }

    @GetMapping("/users/{name}")
    fun user(@PathVariable name: String, model: Model): String {
        model.addAttribute("name", name)
        model.addAttribute("items", listOf("Item 1", "Item 2", "Item 3"))
        return "user"
    }
}