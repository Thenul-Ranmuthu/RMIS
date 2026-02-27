package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.LoginDto;
import com.rmis.rmis.domain.dtos.PublicUserRegisterDto;
import com.rmis.rmis.domain.entities.PublicUser;
import com.rmis.rmis.domain.entities.Role;
import com.rmis.rmis.exceptions.RegisterUserAlreadyExistsException;
import com.rmis.rmis.exceptions.UnregisteredUserException;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.PublicUserRepository;
import com.rmis.rmis.repositories.RoleRepository;
import com.rmis.rmis.services.interfaces.PublicUserAuthService;
import com.rmis.rmis.utils.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PublicUserAuthServiceImpl implements PublicUserAuthService {
    private final PublicUserRepository userRepo;
    private final CompanyRepository companyRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public String publicUserRegistration(PublicUserRegisterDto userDTO){
        if(userRepo.existsByEmail(userDTO.getEmail()) || companyRepo.findByEmail(userDTO.getEmail()).isPresent()){
            throw new RegisterUserAlreadyExistsException("A user with this email already exists");
        }

        Role role = roleRepository.findByName("ROLE_CUSTOMER");
        PublicUser publicUser = new PublicUser();
        publicUser.setEmail(userDTO.getEmail());
        publicUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        publicUser.setFirstName(userDTO.getFname());
        publicUser.setLastName(userDTO.getLname());
        publicUser.setPhone(userDTO.getPhone());
        publicUser.setRole(role);
        publicUser.setIsVerified(true);
        publicUser.setIsActive(true);
        publicUser.setFailedLoginAttempts(0);

        userRepo.save(publicUser);

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userDTO.getEmail(),
                userDTO.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        return token;
    }

    @Override
    public String publicUserLogin(LoginDto userDTO){
        if(userRepo.existsByEmail(userDTO.getEmail())){
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    userDTO.getEmail(),
                    userDTO.getPassword()
            ));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.generateToken(authentication);

            return token;
        } else {
            throw new UnregisteredUserException("User with this email does not exist or password is incorrect.");
        }

    }
}
