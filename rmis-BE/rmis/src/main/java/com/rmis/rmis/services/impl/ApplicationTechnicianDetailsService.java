package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.repositories.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ApplicationTechnicianDetailsService implements UserDetailsService {

    private final TechnicianRepository technicianRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Technician technician = technicianRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Technician not found with email: " + email));

        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(technician.getRole().getName()));

        return new User(
                technician.getEmail(),
                technician.getPassword(),
                authorities
        );
    }
}