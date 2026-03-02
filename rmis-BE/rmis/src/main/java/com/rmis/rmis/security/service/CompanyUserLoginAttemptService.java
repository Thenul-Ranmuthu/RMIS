package com.rmis.rmis.security.service;

import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.security.AbstractLoginAttemptService;
import org.springframework.stereotype.Service;

@Service
public class CompanyUserLoginAttemptService extends AbstractLoginAttemptService<Company> {
    private final CompanyRepository companyRepo;

    public CompanyUserLoginAttemptService(CompanyRepository companyRepo) {
        super(3, 5);
        this.companyRepo = companyRepo;
    }

    @Override
    protected Company save(Company company) {
        return companyRepo.save(company);
    }
}
