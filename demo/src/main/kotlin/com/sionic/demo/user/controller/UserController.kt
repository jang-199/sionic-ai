package com.sionic.demo.user.controller

import com.sionic.demo.config.jwt.TokenProvider
import com.sionic.demo.user.controller.dto.LoginDto
import com.sionic.demo.user.controller.dto.UserRegisterDto
import com.sionic.demo.user.service.UserService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService,
) {
    @PostMapping("/users/register")
    fun register(
        @RequestBody reqeust: UserRegisterDto,
    ): TokenProvider.Token {
        return userService.signUp(reqeust)
    }

    @PostMapping("/users/login")
    fun login(
        @RequestBody reqeust: LoginDto,
    ): TokenProvider.Token {
        return userService.login(reqeust)
    }
}