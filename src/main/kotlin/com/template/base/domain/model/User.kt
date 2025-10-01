package com.template.base.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import com.template.base.domain.model.common.BaseEntity

/**
 * 사용자 엔티티
 * 기본적인 로그인 및 역할 관리 기능을 제공합니다.
 *
 * @property userId 기본키 (자동 증가)
 * @property email 사용자 이메일 (유니크 설정)
 * @property password 사용자 비밀번호 (암호화 필요)
 * @property name 사용자 이름
 * @property role 사용자 역할 (기본값: USER)
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
    val role: Role = Role.USER,

) : BaseEntity() {

    fun copy(
        userId: Long = this.userId,
        email: String = this.email,
        password: String = this.password,
        name: String = this.name,
        role: Role = this.role,
    ) = User(
        userId = userId,
        email = email,
        password = password,
        name = name,
        role = role,
    )
}

/**
 * 사용자 역할을 정의하는 열거형(enum).
 *
 * @property USER 일반 사용자
 * @property ADMIN 관리자
 */
enum class Role {
    USER, ADMIN
}