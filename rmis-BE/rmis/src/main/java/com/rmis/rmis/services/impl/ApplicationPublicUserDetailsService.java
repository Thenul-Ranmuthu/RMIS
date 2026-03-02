package com.rmis.rmis.services.impl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.rmis.rmis.domain.entities.PublicUser;
import com.rmis.rmis.repositories.PublicUserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ApplicationPublicUserDetailsService implements UserDetailsService{
    private PublicUserRepository publicUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        PublicUser publicUser = publicUserRepository.findByEmail(email).
            orElseThrow(() -> new UsernameNotFoundException("user not exists by email"));

        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));

        return new User(
            email, 
            publicUser.getPassword(), 
            authorities
        );
    }


}
