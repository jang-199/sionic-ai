package com.sionic.demo.config.ai

import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AIConfig(
    val aiProperties: AiProperties,
) {
    @Bean
    fun openAiApi(): OpenAiApi {
        return OpenAiApi.builder()
            .apiKey(aiProperties.apiKey)
            .build()
    }
}