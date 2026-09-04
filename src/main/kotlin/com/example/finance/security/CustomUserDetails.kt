package com.example.finance.security

import com.example.finance.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    val id: Long,
    private val usernameStr: String,
    private val passwordHashStr: String
) : UserDetails {

    constructor(user: User) : this(
        id = user.id ?: 0,
        usernameStr = user.username,
        passwordHashStr = user.passwordHash
    )

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun getPassword(): String = passwordHashStr

    override fun getUsername(): String = usernameStr

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
