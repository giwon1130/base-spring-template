package com.template.platform.features.user.domain

import com.template.platform.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction

/**
 * BMOA 사용자 엔티티.
 */
@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userId: Long = 0,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.USER
) : BaseEntity() {

    fun copy(
        userId: Long = this.userId,
        email: String = this.email,
        password: String = this.password,
        name: String = this.name,
        role: Role = this.role
    ) = User(
        userId = userId,
        email = email,
        password = password,
        name = name,
        role = role
    )
}

/**
 * 사용자 역할.
 */
enum class Role {
    USER,
    ADMIN
}
