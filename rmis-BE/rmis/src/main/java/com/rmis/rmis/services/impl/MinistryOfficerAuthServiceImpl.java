package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.domain.dtos.MinistryOfficerRegisterDto;
import com.rmis.rmis.domain.entities.MinistryOfficer;
import com.rmis.rmis.domain.entities.Role;
import com.rmis.rmis.repositories.MinistryOfficerRepository;
import com.rmis.rmis.repositories.RoleRepository;
import com.rmis.rmis.services.interfaces.MinistryOfficerAuthService;
import com.rmis.rmis.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MinistryOfficerAuthServiceImpl implements MinistryOfficerAuthService {
    private JwtTokenProvider jwtTokenProvider;
    private MinistryOfficerRepository officerRepo;
    private RoleRepository roleRepository;
    private AuthenticationProvider officerAuthenticationProvider;
    private final PasswordEncoder passwordEncoder;

    public MinistryOfficerAuthServiceImpl(
            JwtTokenProvider jwtTokenProvider,
            MinistryOfficerRepository officerRepo,
            RoleRepository roleRepository,
            @Qualifier("ministryOfficerAuthenticationProvider")AuthenticationProvider officerAuthenticationProvider,
            PasswordEncoder passwordEncoder

    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.officerRepo = officerRepo;
        this.roleRepository = roleRepository;
        this.officerAuthenticationProvider = officerAuthenticationProvider;
        this.passwordEncoder = passwordEncoder;
    }

//    @Autowired
//    public MinistryOfficerAuthServiceImpl(PasswordEncoder passwordEncoder) {
//        this.passwordEncoder = passwordEncoder;
//    }

    public String ministryOfficerLogin(LoginDto loginRequest) {

        // Authenticates email and password against DB
        Authentication authentication = officerAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // Generates token with userType = "MINISTRY_OFFICER"
        String token = jwtTokenProvider.generateToken(authentication, "MINISTRY_OFFICER");

        return token;
    }

    public String ministryOfficerRegister(MinistryOfficerRegisterDto officerRegisterDto) {
        Role role = roleRepository.findByName("ROLE_MINISTRY_OFFICER");
        MinistryOfficer ministryOfficer = new MinistryOfficer();
        ministryOfficer.setName(officerRegisterDto.getOfficerName());
        ministryOfficer.setEmail(officerRegisterDto.getOfficerEmail());
        ministryOfficer.setPassword(passwordEncoder.encode(officerRegisterDto.getOfficerPassword()));
        ministryOfficer.setRole(role);
        ministryOfficer.setOfficerId(officerRegisterDto.getOfficerId());


        officerRepo.save(ministryOfficer);

        Authentication authentication = officerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(
                officerRegisterDto.getOfficerEmail(),
                officerRegisterDto.getOfficerPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication, "MINISTRY_OFFICER");

        return token;
    }
}
