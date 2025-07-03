package com.sionic.demo.config.jwt

import com.sionic.demo.config.ExcludeAuthPathProperties
import com.sionic.demo.config.jwt.exception.NotFoundTokenException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.http.server.PathContainer
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.pattern.PathPatternParser

@Component
class JwtAuthFilter(
    private val tokenProvider: TokenProvider,
    private val excludeAuthPathProperties: ExcludeAuthPathProperties
) : OncePerRequestFilter() {

    companion object {
        private val pathPatternParser = PathPatternParser()

        private const val TOKEN_HEADER = "Authorization"
        private const val BEARER = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        logger.info { "URL: ${request.method}: ${request.requestURI}, User-Agent: ${request.getHeader("User-Agent")}" }
        logger.info(isExcludedPath(request))
        if (!isExcludedPath(request)) {
            try {
                val token = getToken(request)
                setAuthentication(token)
            } catch (e: Exception) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                return
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun setAuthentication(token: String) {
        tokenProvider.validateToken(token)
        val userId = tokenProvider.extractUserId(token)
        val authorities = setOf(SimpleGrantedAuthority("ROLE_USER"))

        val authentication = UsernamePasswordAuthenticationToken(
            User(userId, "", authorities),
            token,
            authorities
        )
        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun getToken(request: HttpServletRequest): String {
        return request.getHeader(TOKEN_HEADER)
            ?.takeIf { it.startsWith(BEARER) }
            ?.removePrefix(BEARER)
            ?: throw NotFoundTokenException()
    }

    private fun isExcludedPath(request: HttpServletRequest): Boolean {
        val requestPath = request.requestURI
        val requestMethod = HttpMethod.valueOf(request.method)

        return excludeAuthPathProperties.paths.any { authPath ->
            pathPatternParser.parse(authPath.pathPattern)
                .matches(PathContainer.parsePath(requestPath)) &&
                    requestMethod == HttpMethod.valueOf(authPath.method)
        }
    }
}