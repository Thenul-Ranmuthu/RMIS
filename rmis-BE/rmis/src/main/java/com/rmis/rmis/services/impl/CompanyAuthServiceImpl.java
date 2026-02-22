package com.rmis.rmis.services.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rmis.rmis.domain.dtos.CompanyLoginDto;
import com.rmis.rmis.domain.dtos.CompanyRegisterDto;
import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.exceptions.RegisterUserAlreadyExistsException;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.RoleRepository;
import com.rmis.rmis.services.interfaces.CompanyAuthService;
import com.rmis.rmis.utils.JwtTokenProvider;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CompanyAuthServiceImpl implements CompanyAuthService{

    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RoleRepository roleRepository;


    @Override
    public String login(CompanyLoginDto companyLoginDto) {
        
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
            companyLoginDto.getEmail(), 
            companyLoginDto.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);
        
        return token;
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

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
            companyRegisterDto.getEmail(), 
            companyRegisterDto.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        return token;
    }

}
