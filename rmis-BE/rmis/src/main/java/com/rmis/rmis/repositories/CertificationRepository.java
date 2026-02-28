package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.entities.Certification;
import com.rmis.rmis.domain.entities.Technician;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CertificationRepository extends CrudRepository<Certification, Long> {
    List<Certification> findByTechnician(Technician technician);
    List<Certification> findByTechnicianId(Long technicianId);
    void deleteByTechnicianId(Long technicianId);
}