package com.sionic.demo.thread.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.ZonedDateTime

interface ThreadRepository : JpaRepository<Thread, Long> {
    @Query("""
        SELECT t FROM Thread t 
        JOIN t.chats c 
        WHERE t.user.id = :userId 
        GROUP BY t.id 
        HAVING MAX(c.createdAt) >= :cutoff
        ORDER BY MAX(c.createdAt) DESC
    """)
    fun findLatestActiveThread(@Param("userId")userId: Long, cutoff: ZonedDateTime): List<Thread>
}