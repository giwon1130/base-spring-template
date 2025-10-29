package com.template.platform.common.outbox

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Outbox 이벤트 엔티티
 * 
 * 트랜잭션 보장을 위한 Outbox 패턴 구현
 */
@Entity
@Table(
    name = "outbox_events",
    indexes = [
        Index(name = "idx_outbox_status_created", columnList = "status,createdAt"),
        Index(name = "idx_outbox_aggregate", columnList = "aggregateType,aggregateId")
    ]
)
class OutboxEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, length = 100)
    val aggregateType: String,
    
    @Column(nullable = false, length = 100)
    val aggregateId: String,
    
    @Column(nullable = false, length = 100)
    val eventType: String,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val payload: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OutboxStatus = OutboxStatus.PENDING,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column
    var processedAt: LocalDateTime? = null,
    
    @Column
    var retryCount: Int = 0,
    
    @Column(length = 1000)
    var errorMessage: String? = null,
    
    @Version
    var version: Long = 0
) {
    fun markAsProcessed() {
        status = OutboxStatus.PROCESSED
        processedAt = LocalDateTime.now()
    }
    
    fun markAsFailed(error: String) {
        status = OutboxStatus.FAILED
        errorMessage = error.take(1000)
        retryCount++
    }
    
    fun canRetry(maxRetries: Int = 3): Boolean {
        return status == OutboxStatus.FAILED && retryCount < maxRetries
    }
}

/**
 * Outbox 이벤트 상태
 */
enum class OutboxStatus {
    PENDING,    // 대기 중
    PROCESSING, // 처리 중
    PROCESSED,  // 처리 완료
    FAILED      // 처리 실패
}