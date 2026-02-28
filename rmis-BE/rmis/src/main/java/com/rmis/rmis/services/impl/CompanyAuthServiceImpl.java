package com.rmis.rmis.services.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
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

    public CompanyAuthServiceImpl(
        CompanyRepository companyRepository,
        PasswordEncoder passwordEncoder,
        @Qualifier("companyAuthenticationProvider")
        AuthenticationProvider authenticationProvider,
        JwtTokenProvider jwtTokenProvider,
        RoleRepository roleRepository
        ) {
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationProvider = authenticationProvider;
        this.jwtTokenProvider = jwtTokenProvider;
        this.roleRepository = roleRepository;
    }
    

    @Override
    public String login(LoginDto loginDto) {
        if(companyRepository.existsByEmail(loginDto.getEmail())){
            Authentication authentication = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(), 
                loginDto.getPassword()
            ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication, "COMPANY");
        
        return token;
        }else{
            throw new UnregisteredUserException("User with this email does not exist or password is incorrect.");
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
