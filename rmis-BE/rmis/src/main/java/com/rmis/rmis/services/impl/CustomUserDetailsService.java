package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.Technician;
import com.rmis.rmis.repositories.CompanyRepository;
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
public class CustomUserDetailsService implements UserDetailsService {

    private final TechnicianRepository technicianRepository;
    private final CompanyRepository companyRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Check Technician first
        Technician technician = technicianRepository.findByEmail(email).orElse(null);

        if (technician != null) {
            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority(technician.getRole().getName()));

            return new User(
                    technician.getEmail(),
                    technician.getPassword(),
                    authorities
            );
        }

        // Check Company
        Company company = companyRepository.findByEmail(email).orElse(null);

        if (company != null) {
            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority(company.getRole().getName()));

            return new User(
                    company.getEmail(),
                    company.getPassword(),
                    authorities
            );
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}