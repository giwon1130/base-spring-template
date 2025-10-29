package com.template.platform.common.outbox

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

/**
 * Outbox 이벤트 리포지토리
 */
interface OutboxRepository : JpaRepository<OutboxEvent, Long> {
    
    /**
     * 처리 대기 중인 이벤트 조회 (생성 시간 순)
     */
    fun findByStatusOrderByCreatedAt(status: OutboxStatus): List<OutboxEvent>
    
    /**
     * 재시도 가능한 실패 이벤트 조회
     */
    @Query("""
        SELECT e FROM OutboxEvent e 
        WHERE e.status = 'FAILED' 
        AND e.retryCount < :maxRetries
        AND e.createdAt > :since
        ORDER BY e.createdAt
    """)
    fun findRetryableEvents(
        @Param("maxRetries") maxRetries: Int,
        @Param("since") since: LocalDateTime
    ): List<OutboxEvent>
    
    /**
     * 특정 집계체의 이벤트 조회
     */
    fun findByAggregateTypeAndAggregateIdOrderByCreatedAt(
        aggregateType: String,
        aggregateId: String
    ): List<OutboxEvent>
    
    /**
     * 오래된 처리 완료 이벤트 조회 (정리용)
     */
    @Query("""
        SELECT e FROM OutboxEvent e 
        WHERE e.status = 'PROCESSED' 
        AND e.processedAt < :before
    """)
    fun findOldProcessedEvents(@Param("before") before: LocalDateTime): List<OutboxEvent>
    
    /**
     * 처리 중 상태에서 오래된 이벤트 조회 (데드락 방지)
     */
    @Query("""
        SELECT e FROM OutboxEvent e 
        WHERE e.status = 'PROCESSING' 
        AND e.createdAt < :before
    """)
    fun findStuckProcessingEvents(@Param("before") before: LocalDateTime): List<OutboxEvent>
}