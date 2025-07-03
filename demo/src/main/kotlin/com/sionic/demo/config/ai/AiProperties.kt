package com.sionic.demo.config.ai

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.ai.openai")
data class AiProperties(
    val apiKey: String,
)
