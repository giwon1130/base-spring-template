package com.template.base.domain.model.common

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * 생성자와 수정자 정보를 자동으로 관리하는 기본 엔티티
 * BaseTimeEntity를 확장하여 시간 정보와 사용자 정보를 모두 관리합니다.
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