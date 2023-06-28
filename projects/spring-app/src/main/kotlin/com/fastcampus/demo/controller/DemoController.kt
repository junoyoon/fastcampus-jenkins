package com.fastcampus.demo.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.io.File

@RestController
class DemoController(
    @Value("\${application.git-commit:}")
    val gitCommit: String,
    @Value("\${application.branch:}")
    val branch: String
) {
    val aboutToClose = "${System.getProperty("user.home")}/about-to-shutdown"

    @GetMapping("/")
    fun home(): Map<String, String> {
        return mapOf(
            "version" to "1.0",
            "gitCommit" to gitCommit,
            "branch" to branch,
            "hello" to "world"
        )
    }

    @GetMapping("/health")
    fun health(): Map<String, String> {
        if (File(aboutToClose).exists()) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "about to close"
            )
        }
        return mapOf(
            "status" to "OK"
        )
    }
}
