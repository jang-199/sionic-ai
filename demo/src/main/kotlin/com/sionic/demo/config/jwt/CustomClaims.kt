package com.sionic.demo.config.jwt

data class CustomClaims(
    val claims: Map<String, String>
) {
    constructor(userId: Long, tokenType: TokenType) : this(
        mapOf(
            "userId" to userId.toString(),
            "tokenType" to tokenType.toString()
        )
    )
}