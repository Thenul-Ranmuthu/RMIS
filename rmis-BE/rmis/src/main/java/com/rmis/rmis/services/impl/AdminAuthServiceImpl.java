package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.AdminRegisterDto;
import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.domain.entities.Admin;
import com.rmis.rmis.domain.entities.MinistryOfficer;
import com.rmis.rmis.domain.entities.Role;
import com.rmis.rmis.exceptions.RegisterUserAlreadyExistsException;
import com.rmis.rmis.repositories.AdminRepository;
import com.rmis.rmis.repositories.RoleRepository;
import com.rmis.rmis.services.interfaces.AdminAuthService;
import com.rmis.rmis.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private JwtTokenProvider jwtTokenProvider;
    private AdminRepository adminRepository;
    private RoleRepository roleRepository;
    private AuthenticationProvider adminAuthenticationProvider;
    private final PasswordEncoder passwordEncoder;

    public AdminAuthServiceImpl(
            JwtTokenProvider jwtTokenProvider,
            AdminRepository adminRepository,
            RoleRepository roleRepository,
            @Qualifier("adminAuthenticationProvider")AuthenticationProvider adminAuthenticationProvider,
            PasswordEncoder passwordEncoder

    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.adminRepository = adminRepository;
        this.roleRepository = roleRepository;
        this.adminAuthenticationProvider = adminAuthenticationProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public String adminLogin(LoginDto loginRequest) {

        // Authenticates email and password against DB
        Authentication authentication = adminAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        String token = jwtTokenProvider.generateToken(authentication, "ADMIN");

        return token;
    }

    public String adminRegister(AdminRegisterDto adminRegisterDto) {
        if(adminRepository.existsByEmail(adminRegisterDto.getAdminEmail())){
            throw new RegisterUserAlreadyExistsException("Admin with this email already exists");
        }

        Role role = roleRepository.findByName("ROLE_ADMIN");
        Admin admin = new Admin();
        admin.setName(adminRegisterDto.getAdminName());
        admin.setEmail(adminRegisterDto.getAdminEmail());
        admin.setPassword(passwordEncoder.encode(adminRegisterDto.getAdminPassword()));
        admin.setRole(role);

        adminRepository.save(admin);

        Authentication authentication = adminAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(
                adminRegisterDto.getAdminEmail(),
                adminRegisterDto.getAdminPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication, "ADMIN");

        return token;
    }
}
