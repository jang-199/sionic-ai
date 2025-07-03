package com.sionic.demo.config

import com.sionic.demo.config.jwt.JwtAuthFilter
import com.sionic.demo.config.jwt.TokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val tokenProvider: TokenProvider,
    private val excludeAuthPathProperties: ExcludeAuthPathProperties
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        http.authorizeHttpRequests { authz ->
            excludeAuthPathProperties.paths.iterator().forEach { authPath ->
                authz.requestMatchers(
                    HttpMethod.valueOf(authPath.method),
                    authPath.pathPattern
                ).permitAll()
            }
            authz.anyRequest().authenticated()
        }

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)

        http.exceptionHandling {
            it.authenticationEntryPoint { request, response, authException ->
                response.sendError(response.status, "토큰 오류")
            }
        }

        return http.build()
    }

    @Bean
    fun jwtAuthenticationFilter(): JwtAuthFilter {
        return JwtAuthFilter(tokenProvider, excludeAuthPathProperties)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}