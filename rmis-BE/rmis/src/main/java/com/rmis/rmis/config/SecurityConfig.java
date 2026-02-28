package com.rmis.rmis.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.rmis.rmis.utils.JwtAuthenticationEntryPoint;
import com.rmis.rmis.utils.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// ...existing code...

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")//http://localhost:3000
    private String allowedOriginsProperty;
    

    // ---- Inject both UserDetailsService implementations ----

    private final UserDetailsService companyDetailsService;
    // private final UserDetailsService publicUserDetailsService;

    public SecurityConfig(
            @Qualifier("applicationCompanyDetailsService") UserDetailsService companyDetailsService
            // @Qualifier("applicationPublicUserDetailsService") UserDetailsService publicUserDetailsService
    ) {
        this.companyDetailsService = companyDetailsService;
        // this.publicUserDetailsService = publicUserDetailsService;
    }

    // ---- Authentication Providers ----

    @Bean
    public AuthenticationProvider companyAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(companyDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // @Bean
    // public AuthenticationProvider publicUserAuthenticationProvider() {
    //     DaoAuthenticationProvider provider = new DaoAuthenticationProvider(publicUserDetailsService);
    //     provider.setPasswordEncoder(passwordEncoder());
    //     return provider;
    // }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {}) // enables CORS support using corsConfigurationSource bean
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/company/**").permitAll()
                .requestMatchers("/sendMail/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        // Register our JWT filter before the UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // allow multiple origins via comma-separated property
        configuration.setAllowedOrigins(
            Arrays.stream(allowedOriginsProperty.split(","))
                  .map(String::trim)
                  .collect(Collectors.toList())
        );
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
