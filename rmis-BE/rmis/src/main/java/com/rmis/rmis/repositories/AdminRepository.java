package com.rmis.rmis.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rmis.rmis.domain.entities.Admin;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>{
    Boolean existsByEmail(String email);
    Optional<Admin> findByEmail(String email);
}
