package com.fastcampus.demo

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DemoApplicationTests(
    @Value("\${spring.datasource.url}")
    val dataSourceUrl: String
) {

    @Test
    fun contextLoads() {
        // empty
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SLOW_TEST_ENABLED", matches = "true")
    fun slowTest() {
        println("************************************")
        println("slow test running")
        Thread.sleep(1000 * 30)
        println("slow test finished")
        println("************************************")
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SPRING_PROFILES_ACTIVE", matches = ".*")
    fun dataSourceUrlTest() {
        println("************************************")
        println("spring.datasource.url $dataSourceUrl")
        println("************************************")
    }
}
