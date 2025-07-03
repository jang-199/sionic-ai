package com.sionic.demo

import com.sionic.demo.config.jwt.JWTProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
//@EnableConfigurationProperties(JWTProperties::class)
@ConfigurationPropertiesScan
@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}
