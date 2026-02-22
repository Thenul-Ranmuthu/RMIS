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

import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.repositories.CompanyRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ApplicationCompanyDetailsService implements UserDetailsService{

    private CompanyRepository companyRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Company company = companyRepository.findByEmail(email).
            orElseThrow(() -> new UsernameNotFoundException("Company not exists by email"));

        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_COMPANY"));

        return new User(
            email,
            company.getPassword(),
            authorities
        );
    }
    
}
