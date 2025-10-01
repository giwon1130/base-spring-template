package com.template.base.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.template.base.domain.model.User
import java.util.Optional

/**
 * 사용자 정보를 관리하는 Repository 인터페이스
 */
@Repository
interface UserRepository : JpaRepository<User, Long> {

    /**
     * 이메일을 기준으로 사용자 정보를 조회하는 메서드
     */
    fun findByEmail(email: String): Optional<User>

    /**
     * 이메일 존재 여부를 확인하는 메서드
     */
    fun existsByEmail(email: String): Boolean
}