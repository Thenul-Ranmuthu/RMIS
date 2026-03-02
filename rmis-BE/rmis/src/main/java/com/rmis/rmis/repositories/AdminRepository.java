package com.rmis.rmis.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rmis.rmis.domain.entities.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>{

}
