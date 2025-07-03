package com.sionic.demo.config.jwt

import com.sionic.demo.config.jwt.exception.InvalidTokenException
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Deserializer
import io.jsonwebtoken.jackson.io.JacksonDeserializer
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import javax.crypto.SecretKey

@Component
class TokenProvider(
    private val jwtProperties: JWTProperties
) {
    private val issuer = "clip"
    private val zoneId = ZoneId.of("Asia/Seoul")

    fun isValidRefreshToken(token: String): Boolean {
        return try {
            val tokenType = initParser()
                .parseSignedClaims(token)
                .payload["tokenType"] as String?
            tokenType == TokenType.REFRESH_TOKEN.toString()
        } catch (e: Exception) {
            false
        }
    }

    fun generateToken(userId: Long, currentTime: LocalDateTime): Token {
        return Token(
            accessToken = generateAccessToken(userId, currentTime),
            refreshToken = generateRefreshToken(userId, currentTime)
        )
    }

    fun generateAccessToken(userId: Long, currentDateTime: LocalDateTime): String {
        return generateTokenInternal(
            CustomClaims(userId, TokenType.ACCESS_TOKEN),
            currentDateTime,
            currentDateTime.plusDays(jwtProperties.accessTokenExpirationPeriodDay.toLong())
        )
    }

    fun generateRefreshToken(userId: Long, currentDateTime: LocalDateTime): String {
        return generateTokenInternal(
            CustomClaims(userId, TokenType.REFRESH_TOKEN),
            currentDateTime,
            currentDateTime.plusMonths(jwtProperties.refreshTokenExpirationPeriodMonth.toLong())
        )
    }

    fun reissueToken(userId: Long, refreshToken: String): Token {
        val now = LocalDateTime.now()
        return Token(
            accessToken = generateAccessToken(userId, now),
            refreshToken = generateTokenInternal(
                CustomClaims(userId, TokenType.REFRESH_TOKEN),
                now,
                getExpirationToLocalDateTime(refreshToken)
            )
        )
    }

    fun extractUserId(token: String): String {
        return initParser()
            .parseSignedClaims(token)
            .payload["userId"] as String
    }

    fun validateToken(token: String) {
        try {
            initParser().parse(token)
        } catch (e: Exception) {
            throw InvalidTokenException()
        }
    }

    private fun getExpirationToLocalDateTime(token: String): LocalDateTime {
        return try {
            val expiration = initParser()
                .parseSignedClaims(token)
                .payload.expiration
            expiration.toInstant().atZone(zoneId).toLocalDateTime()
        } catch (e: Exception) {
            throw InvalidTokenException()
        }
    }

    private fun initParser(): JwtParser {
        return Jwts.parser()
            .json(JacksonDeserializer<MutableMap<String, Any>>() as Deserializer<Map<String?, *>?>?)
            .verifyWith(getSigningKey())
            .requireIssuer(issuer)
            .build()
    }

    private fun generateTokenInternal(
        customClaims: CustomClaims,
        issuedAt: LocalDateTime,
        expiresAt: LocalDateTime
    ): String {
        return Jwts.builder()
            .issuer(issuer)
            .claims(customClaims.claims)
            .issuedAt(Date.from(issuedAt.atZone(zoneId).toInstant()))
            .expiration(Date.from(expiresAt.atZone(zoneId).toInstant()))
            .signWith(getSigningKey())
            .compact()
    }

    private fun getSigningKey(): SecretKey {
        return Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray(StandardCharsets.UTF_8))
    }

    data class Token(val accessToken: String, val refreshToken: String)

    data class AccessToken(val accessToken: String)

    data class RefreshToken(val refreshToken: String)
}