package com.rmis.rmis.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

// Execute Before Executing Spring Security Filters
// Validate the JWT Token and Provides user details to Spring Security for Authentication
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    @Qualifier("applicationCompanyDetailsService") 
    UserDetailsService applicationCompanyDetailsService;

    @Autowired
    @Qualifier("applicationPublicUserDetailsService")
    UserDetailsService applicationPublicUserDetailsService;

    @Autowired
    @Qualifier("applicationTechnicianDetailsService")
    UserDetailsService applicationTechnicianUserDetailsService;

    @Autowired
    @Qualifier("applicationAdminDetailsService")
    UserDetailsService applicationAdminDetailsService;

    @Autowired
    @Qualifier("applicationMinistryOfficerDetailsService")
    UserDetailsService applicationMinistryOfficerDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsername(token);
            String userType = jwtTokenProvider.getUserType(token);
            
            log.info("JWT Filter: Valid token for user: {}, type: {}", username, userType);

            if (username != null) {
                try {
                    UserDetails userDetails = loadUser(token, username);

                    if (userDetails != null) {
                        log.info("JWT Filter: Successfully loaded user details for {}", username);
                        UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                        authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    } else {
                        log.error("JWT Filter: User details were NULL for {}", username);
                    }
                } catch (Exception e) {
                    log.error("JWT Filter: Failed to load user details for {}: {}", username, e.getMessage());
                }
            }
        } else if (StringUtils.hasText(token)) {
            log.warn("JWT Filter: Token was present but FAILED validation.");
        }

        filterChain.doFilter(request, response);
    }

    private UserDetails loadUser(String token, String username) {
        // Route to correct service based on JWT userType claim
        String userType = jwtTokenProvider.getUserType(token);

        if ("COMPANY".equals(userType)) {
            return applicationCompanyDetailsService.loadUserByUsername(username);
        } else if("PUBLIC".equals(userType)){
            return applicationPublicUserDetailsService.loadUserByUsername(username);
        } else if("MINISTRY_OFFICER".equals(userType)) {
            return applicationMinistryOfficerDetailsService.loadUserByUsername(username);
        } else if("ADMIN".equals(userType)) {
            return applicationAdminDetailsService.loadUserByUsername(username);
        }else{
            return applicationTechnicianUserDetailsService.loadUserByUsername(username);
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

