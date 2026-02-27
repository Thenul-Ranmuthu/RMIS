package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.PublicUser;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.PublicUserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CompanyRepository companyRepository;
    private final PublicUserRepository publicUserRepository;
//    private final TechnicianRepository technicianRepository;

    @Autowired
    public CustomUserDetailsService(
            CompanyRepository companyRepository,
            PublicUserRepository publicUserRepository) {

        this.companyRepository = companyRepository;
        this.publicUserRepository = publicUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1️⃣ Check Company
        Company company = companyRepository.findByEmail(email).orElse(null);
        if (company != null) {

            return buildUser(company.getEmail(),
                    company.getPassword(),
                    "ROLE_COMPANY");
        }

        // 2️⃣ Check Public User
        PublicUser publicUser = publicUserRepository.findByEmail(email).orElse(null);
        if (publicUser != null) {
            return buildUser(publicUser.getEmail(),
                    publicUser.getPassword(),
                    "ROLE_CUSTOMER");
        }

//        // 3️⃣ Check Technician
//        Technician technician = technicianRepository.findByUsername(email).orElse(null);
//        if (technician != null) {
//            return buildUser(technician.getUsername(),
//                    technician.getPassword(),
//                    "ROLE_TECHNICIAN");
//        }

        throw new UsernameNotFoundException("User not found with username: " + email);
    }

    private UserDetails buildUser(String username,
                                  String password,
                                  String role) {

        Collection<? extends GrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority(role));

        return new User(username, password, authorities);
    }
}