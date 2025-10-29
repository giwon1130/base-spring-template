package com.template.platform.common.outbox

/**
 * Outbox 이벤트 발행자 인터페이스
 * 
 * 실제 메시징 시스템(Kafka 등)에 이벤트를 발행하는 책임
 */
interface OutboxPublisher {
    
    /**
     * 이벤트를 외부 시스템에 발행
     * 
     * @param event 발행할 Outbox 이벤트
     * @return 발행 성공 여부
     */
    fun publish(event: OutboxEvent): PublishResult
}

/**
 * 발행 결과
 */
sealed class PublishResult {
    object Success : PublishResult()
    data class Failure(val error: String) : PublishResult()
}

/**
 * 기본 구현체 (로그만 출력)
 */
class LoggingOutboxPublisher : OutboxPublisher {
    override fun publish(event: OutboxEvent): PublishResult {
        println("📤 Publishing outbox event: ${event.eventType} for ${event.aggregateType}:${event.aggregateId}")
        return PublishResult.Success
    }
}