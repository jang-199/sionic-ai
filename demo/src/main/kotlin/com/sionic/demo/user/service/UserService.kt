package com.sionic.demo.user.service

import com.sionic.demo.config.jwt.TokenProvider
import com.sionic.demo.user.controller.dto.LoginDto
import com.sionic.demo.user.controller.dto.UserRegisterDto
import com.sionic.demo.user.repository.Role
import com.sionic.demo.user.repository.User
import com.sionic.demo.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserService(
    private val repository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: TokenProvider
) {
    @Transactional
    fun signUp(
        userRegisterDto: UserRegisterDto,
    ): TokenProvider.Token{
        if (repository.existsByEmail(userRegisterDto.email)) {
            throw IllegalStateException("User already registered")
        }

        val encodedPassword = passwordEncoder.encode(userRegisterDto.password)

        val newUser = User(
            email = userRegisterDto.email,
            password = encodedPassword,
            role = Role.MEMBER,
            name = userRegisterDto.username,
        )

        val userId = repository.save(newUser).id ?: throw IllegalStateException("User ID is null")

        return tokenProvider.generateToken(userId, LocalDateTime.now())
    }

    fun login(
        loginDto: LoginDto,
    ): TokenProvider.Token {
        val user =
            repository.findByEmail(loginDto.email).orElseThrow { throw IllegalArgumentException("user not found") }

        if (passwordEncoder.matches(loginDto.password, user.password)) {
            return tokenProvider.generateToken(user.id ?: throw IllegalStateException("User ID is null"), LocalDateTime.now())
        } else {
            throw IllegalArgumentException("Invalid password")
        }
    }
}