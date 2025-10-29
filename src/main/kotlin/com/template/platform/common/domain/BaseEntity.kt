package com.template.platform.common.domain

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * 감사 정보(createdBy/lastModifiedBy)를 포함한 공통 상위 엔티티.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class BaseEntity : BaseTimeEntity() {

    @CreatedBy
    @Column(updatable = false)
    open var createdBy: Long? = null

    @LastModifiedBy
    @Column(name = "last_modified_by")
    open var lastModifiedBy: Long? = null
}
