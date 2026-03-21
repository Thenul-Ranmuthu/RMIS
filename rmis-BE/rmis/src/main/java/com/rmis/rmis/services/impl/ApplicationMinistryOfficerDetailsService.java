package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.entities.MinistryOfficer;
import com.rmis.rmis.repositories.MinistryOfficerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("applicationMinistryOfficerDetailsService")
public class ApplicationMinistryOfficerDetailsService implements UserDetailsService {

    @Autowired
    private MinistryOfficerRepository ministryOfficerRepository;  // your officer repository

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MinistryOfficer officer = ministryOfficerRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Officer not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                officer.getEmail(),
                officer.getPassword(),
                List.of(new SimpleGrantedAuthority(officer.getRole().getName()))  // 👈 must return role
        );
    }
}
