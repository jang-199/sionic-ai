package com.sionic.demo.thread.controller

import com.sionic.demo.thread.repository.Chat
import com.sionic.demo.thread.service.ChatService
import com.sionic.demo.user.repository.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping
    fun ask(
        @RequestParam question: String,
        @RequestParam(required = false) isStreaming: Boolean = false,
        @RequestParam(required = false) model: String? = null,
        @AuthenticationPrincipal user: UserDetails
    ): ResponseEntity<Chat> {
        println(user.username)
        val chat = chatService.chat(1L, question, isStreaming, model)
        return ResponseEntity.ok(chat)
    }

    @GetMapping("/{threadId}")
    fun getThreadChats(
        @PathVariable threadId: Long,
        @AuthenticationPrincipal user: User,
        pageable: Pageable
    ): Page<Chat> {
        return chatService.listChats(user, threadId, pageable)
    }

    @DeleteMapping("/threads/{threadId}")
    fun deleteThread(
        @PathVariable threadId: Long,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Void> {
        chatService.deleteThread(user, threadId)
        return ResponseEntity.noContent().build()
    }
}