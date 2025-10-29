package com.template.platform.common.domain

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

/**
 * 생성/수정/삭제 시각을 추적하는 공통 상위 클래스.
 */
@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
open class BaseTimeEntity {

    @CreatedDate
    @Column(updatable = false)
    open var createdAt: Instant? = null

    @LastModifiedDate
    open var lastModifiedAt: Instant? = null

    @Column(updatable = false)
    open var deletedAt: Instant? = null
}
