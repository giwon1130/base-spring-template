package com.template.base.domain.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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

    /**
     * 삭제된 사용자를 포함한 모든 사용자 조회 (관리자용)
     */
    @Query("SELECT u FROM User u")
    fun findAllIncludingDeleted(pageable: Pageable): Page<User>

    /**
     * 삭제된 사용자를 포함한 특정 사용자 조회 (관리자용)
     */
    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    fun findByIdIncludingDeleted(@Param("userId") userId: Long): User?

    /**
     * 이메일 또는 이름으로 사용자 검색 (삭제된 사용자 포함)
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE %:search% OR u.name LIKE %:search%")
    fun findByEmailContainingOrNameContainingIncludingDeleted(
        @Param("search") email: String,
        @Param("search") name: String,
        pageable: Pageable
    ): Page<User>
}