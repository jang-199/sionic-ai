package com.sionic.demo.config.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JWTProperties (
    val secretKey: String,
    val accessTokenExpirationPeriodDay: Int,
    val refreshTokenExpirationPeriodMonth: Int
)