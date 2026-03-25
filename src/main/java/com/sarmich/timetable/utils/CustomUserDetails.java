package com.sarmich.timetable.utils;

import com.sarmich.timetable.domain.ProfileEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private Integer id;
    private ProfileEntity profileEntity;

    public CustomUserDetails(Integer id) {
        this.id = id;
    }

    public CustomUserDetails(ProfileEntity profileEntity) {
        this.profileEntity = profileEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = (profileEntity != null) ? profileEntity.getRole().name() : null;
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(role);
        return Collections.singletonList(simpleGrantedAuthority);
    }

    @Override
    public String getPassword() {
        return (profileEntity != null) ? profileEntity.getPassword() : "";
    }

    @Override
    public String getUsername() {
        return (profileEntity != null) ? profileEntity.getEmail() : "";
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
        return true;
    }

    public ProfileEntity getProfileEntity() {
        return profileEntity;
    }
}
