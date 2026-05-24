package com.kilimo.ticket.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {
    private User user;
    
    public UserPrincipal(User user) {
        this.user = user;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Handle null role - default to USER role if not assigned
        if (user.getRole() == null) {
            return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        }
        String normalizedRole = user.getRole().getName() == null
            ? "USER"
            : user.getRole().getName().trim().toUpperCase(Locale.ROOT);
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + normalizedRole));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(user.getStatus());
    }
}
