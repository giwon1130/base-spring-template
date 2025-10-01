package com.template.base.infrastructure.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import com.template.base.domain.model.User

class CustomUserDetails(
    private val user: User
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList() // 권한 처리 (필요 시 추가)

    override fun getPassword(): String = user.password // 비밀번호 반환

    override fun getUsername(): String = user.email // Spring Security에서 사용하는 유저 식별값 (email 사용)

    override fun isAccountNonExpired(): Boolean = true // 계정 만료 여부 (true: 만료되지 않음)

    override fun isAccountNonLocked(): Boolean = true // 계정 잠김 여부 (true: 잠기지 않음)

    override fun isCredentialsNonExpired(): Boolean = true // 비밀번호 만료 여부 (true: 만료되지 않음)

    override fun isEnabled(): Boolean = true // 계정 활성화 여부 (true: 활성화됨)

    fun getUserId(): Long = user.userId // 사용자 ID 반환

    fun getUserEmail(): String = user.email // 사용자 이메일 반환
}