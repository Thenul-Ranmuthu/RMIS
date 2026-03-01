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
import com.rmis.rmis.security.interfaces.LoginAttemptService;
import com.rmis.rmis.services.interfaces.PublicUserAuthService;
import com.rmis.rmis.utils.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PublicUserAuthServiceImpl implements PublicUserAuthService {
    private final PublicUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RoleRepository roleRepository;
    private final AuthenticationProvider authenticationProvider;
    private final LoginAttemptService loginAttemptService;

    public PublicUserAuthServiceImpl(
            PublicUserRepository userRepo,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RoleRepository roleRepository,
            @Qualifier("publicUserAuthenticationProvider") AuthenticationProvider authenticationProvider,
            LoginAttemptService<PublicUser> loginAttemptService

    ) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.roleRepository = roleRepository;
        this.authenticationProvider = authenticationProvider;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    @Transactional
    public String publicUserRegistration(PublicUserRegisterDto userDTO){
        if(userRepo.existsByEmail(userDTO.getEmail())){
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

        Authentication authentication = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(
                userDTO.getEmail(),
                userDTO.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);

        return token;
    }

    @Override
    //@Transactional
    public String publicUserLogin(LoginDto userDTO){
        // 1) Find user (needed to update attempts/lockedUntil)
        PublicUser user = userRepo.findByEmail(userDTO.getEmail())
                // 2) don’t reveal existence
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
        System.out.println("User: " + user.toString());

        // 3 + 4) check lock / reset if lock expired
        loginAttemptService.preAuthenticateCheck(user);
        System.out.println("pre authenticated");

        try {
            // 5) authenticate
            Authentication authentication = authenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userDTO.getEmail(),
                            userDTO.getPassword()
                    )
            );

            // 6) success -> reset attempts
            loginAttemptService.onSuccess(user);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token =  jwtTokenProvider.generateToken(authentication);
            System.out.println("Authentication successful");
            return token;

        } catch (BadCredentialsException ex) {

            // 7) failure -> increment attempts + lock if needed
            loginAttemptService.onFailure(user);

            int remaining = loginAttemptService.remainingAttempts(user);
            throw new BadCredentialsException("Bad credentials. Remaining attempts: " + remaining);
        }

    }
}
