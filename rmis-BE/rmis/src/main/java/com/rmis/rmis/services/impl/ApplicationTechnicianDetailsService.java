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

import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.repositories.TechnicianRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ApplicationTechnicianDetailsService implements UserDetailsService{

    private TechnicianRepository technicianRepository;
    
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Technician technician = technicianRepository.findByEmail(email).
            orElseThrow(() -> new UsernameNotFoundException("user not exists by email"));

        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_TECHNITIAN"));

        return new User(
            email, 
            technician.getPassword(), 
            authorities
        );
    }
    
}
