package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.entities.PublicUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublicUserRepository extends JpaRepository<PublicUser, Long> {
        Optional<PublicUser> findByEmail(String email);
        Boolean existsByEmail(String email);
}
