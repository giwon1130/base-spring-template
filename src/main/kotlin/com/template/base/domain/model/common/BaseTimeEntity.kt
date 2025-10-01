package com.template.base.domain.model.common

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

/**
 * 생성일시와 수정일시를 자동으로 관리하는 기본 엔티티
 * 모든 엔티티는 이 클래스를 상속받아 시간 정보를 자동으로 관리할 수 있습니다.
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