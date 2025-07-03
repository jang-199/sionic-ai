package com.sionic.demo.thread.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRepository : JpaRepository<Chat, Long> {
    fun findByThreadIdOrderByCreatedAtDesc(threadId: Long, pageable: Pageable): Page<Chat>
}