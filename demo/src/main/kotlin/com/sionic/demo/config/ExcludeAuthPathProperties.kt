package com.sionic.demo.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("exclude-auth-path-patterns")
data class ExcludeAuthPathProperties(
    val paths: List<AuthPath>
) {
    fun getExcludeAuthPaths(): List<String> =
        paths.map { it.pathPattern }

    data class AuthPath(
        val pathPattern: String,
        val method: String
    )
}