package com.rmis.rmis.services.impl;

import com.rmis.rmis.security.interfaces.LoginAttemptService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.domain.dtos.CompanyRegisterDto;
import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.exceptions.RegisterUserAlreadyExistsException;
import com.rmis.rmis.exceptions.UnregisteredUserException;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.RoleRepository;
import com.rmis.rmis.services.interfaces.CompanyAuthService;
import com.rmis.rmis.utils.JwtTokenProvider;


@Service
public class CompanyAuthServiceImpl implements CompanyAuthService{

    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationProvider authenticationProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final RoleRepository roleRepository;
    private final LoginAttemptService<Company> loginAttemptService;

    public CompanyAuthServiceImpl(
        CompanyRepository companyRepository,
        PasswordEncoder passwordEncoder,
        @Qualifier("companyAuthenticationProvider")
        AuthenticationProvider authenticationProvider,
        JwtTokenProvider jwtTokenProvider,
        RoleRepository roleRepository,
        LoginAttemptService<Company> loginAttemptService
        ) {
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationProvider = authenticationProvider;
        this.jwtTokenProvider = jwtTokenProvider;
        this.roleRepository = roleRepository;
        this.loginAttemptService = loginAttemptService;
    }
    

    @Override
    public String login(LoginDto loginDto) {
        Company company = companyRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Bad Credentials"));

        loginAttemptService.preAuthenticateCheck(company);
        try {
            Authentication authentication = authenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()
                    )
            );

            loginAttemptService.onSuccess(company);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token =  jwtTokenProvider.generateToken(authentication, "COMPANY");
            return token;

        } catch (BadCredentialsException ex) {

            loginAttemptService.onFailure(company);

            int remaining = loginAttemptService.remainingAttempts(company);
            throw new BadCredentialsException("Bad credentials. Remaining attempts: " + remaining);
        }
        
    }

    

    @Override
    public String register(CompanyRegisterDto companyRegisterDto) {

        if(companyRepository.findByEmail(companyRegisterDto.getEmail()).isPresent()){
            throw new RegisterUserAlreadyExistsException("Email already exists!!");
        }

        Company company = new Company();
        company.setCompanyid(companyRegisterDto.getCompanyid());
        company.setEmail(companyRegisterDto.getEmail());
        company.setName(companyRegisterDto.getName());
        company.setPassword(passwordEncoder.encode(companyRegisterDto.getPassword()));
        company.setRole(roleRepository.findByName("ROLE_COMPANY"));
        company.setFailedLoginAttempts(0);

        companyRepository.save(company);

        Authentication authentication = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(
            companyRegisterDto.getEmail(), 
            companyRegisterDto.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication, "COMPANY");

        return token;
    }

}
