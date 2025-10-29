package com.template.platform.bootstrap.security

import com.template.platform.features.user.domain.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Spring Security와 BMOA 사용자 엔티티를 연결하는 어댑터.
 */
class CustomUserDetails(
    private val user: User
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList()

    override fun getPassword(): String = user.password

    override fun getUsername(): String = user.email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    fun getUserId(): Long = user.userId

    fun getUserEmail(): String = user.email
}
