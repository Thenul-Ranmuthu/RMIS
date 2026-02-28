package com.rmis.rmis.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rmis.rmis.domain.entities.Role;
import com.rmis.rmis.repositories.RoleRepository;


@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initData(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName("ROLE_CUSTOMER") == null) {
                roleRepository.save(new Role(null, "ROLE_CUSTOMER"));
            }
            if (roleRepository.findByName("ROLE_ADMIN") == null) {
                roleRepository.save(new Role(null, "ROLE_ADMIN"));
            }
            if(roleRepository.findByName("ROLE_COMPANY") == null){
                roleRepository.save(new Role(null, "ROLE_COMPANY"));
            }
            if(roleRepository.findByName("ROLE_TECHNICIAN") == null){
                roleRepository.save(new Role(null, "ROLE_TECHNICIAN"));
            }
        };
    }
}
