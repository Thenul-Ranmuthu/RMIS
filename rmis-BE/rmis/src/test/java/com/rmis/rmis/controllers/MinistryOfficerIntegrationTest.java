package com.rmis.rmis.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.MinistryOfficer;
import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.domain.entities.Role;
import com.rmis.rmis.enums.CompanyStatus;
import com.rmis.rmis.enums.QuotaRequestStatus;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.MinistryOfficerRepository;
import com.rmis.rmis.repositories.QuotaRequestRepository;
import com.rmis.rmis.repositories.RoleRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class MinistryOfficerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuotaRequestRepository quotaRequestRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private MinistryOfficerRepository ministryOfficerRepository;

    @Autowired
    private RoleRepository roleRepository;

    private QuotaRequest testRequest;

    @BeforeEach
    void setUp() {
        // Clean up before starting
        quotaRequestRepository.deleteAll();
        ministryOfficerRepository.deleteAll();
        companyRepository.deleteAll();
        roleRepository.deleteAll();

        // 1. Create Roles
        Role companyRole = new Role(null, "COMPANY");
        Role officerRole = new Role(null, "MINISTRY_OFFICER");
        roleRepository.save(companyRole);
        roleRepository.save(officerRole);

        // 2. Create Company
        Company company = new Company();
        company.setName("Integration Test Company");
        company.setEmail("company@test.com");
        company.setCompanyid("COMP123");
        company.setPassword("password");
        company.setRole(companyRole);
        company.setQuota(new BigDecimal("5000.00"));
        company.setStatus(CompanyStatus.ACTIVE);
        company = companyRepository.save(company);

        // 3. Create Ministry Officer
        MinistryOfficer officer = new MinistryOfficer();
        officer.setName("Integration Officer");
        officer.setEmail("officer@test.com");
        officer.setOfficerId("OFFICER123");
        officer.setPassword("password");
        officer.setRole(officerRole);
        ministryOfficerRepository.save(officer);

        // 4. Create Quota Request
        testRequest = new QuotaRequest();
        testRequest.setCompany(company);
        testRequest.setCompanyName(company.getName());
        testRequest.setRequestedQuota(new BigDecimal("1000.00"));
        testRequest.setStatus(QuotaRequestStatus.PENDING);
        testRequest.setRequestNumber(1001L);
        testRequest = quotaRequestRepository.save(testRequest);
    }
    
    @AfterEach
    void tearDown() {
        quotaRequestRepository.deleteAll();
        ministryOfficerRepository.deleteAll();
        companyRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "officer@test.com", roles = "MINISTRY_OFFICER")
    void shouldApprovePendingQuotaRequest() throws Exception {
        UUID requestId = testRequest.getRequestId();

        mockMvc.perform(patch("/ministry/statusApprove/" + requestId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Status set to APPROVED"));

        // Verify Database State
        QuotaRequest updatedRequest = quotaRequestRepository.findById(requestId).orElseThrow();
        assertEquals(QuotaRequestStatus.APPROVED, updatedRequest.getStatus());
        
        Company updatedCompany = companyRepository.findByEmail("company@test.com").orElseThrow();
        assertEquals(0, new BigDecimal("4000.00").compareTo(updatedCompany.getQuota()));
    }

    @Test
    @WithMockUser(username = "officer@test.com", roles = "MINISTRY_OFFICER")
    void shouldRejectPendingQuotaRequest() throws Exception {
        UUID requestId = testRequest.getRequestId();
        String reason = "Quota request too high";

        mockMvc.perform(patch("/ministry/statusReject/" + requestId)
                .content(reason)
                .contentType(MediaType.TEXT_PLAIN)) // Reason as raw string body
                .andExpect(status().isOk())
                .andExpect(content().string("Status set to REJECTED"));

        // Verify Database State
        QuotaRequest updatedRequest = quotaRequestRepository.findById(requestId).orElseThrow();
        assertEquals(QuotaRequestStatus.REJECTED, updatedRequest.getStatus());
        assertEquals(reason, updatedRequest.getRejectionReason());
        
        Company updatedCompany = companyRepository.findByEmail("company@test.com").orElseThrow();
        assertEquals(0, new BigDecimal("5000.00").compareTo(updatedCompany.getQuota()));
    }

    @Test
    @WithMockUser(username = "officer@test.com", roles = "MINISTRY_OFFICER")
    void shouldFailToApproveAlreadyProcessedRequest() throws Exception {
        // Change status to APPROVED first
        testRequest.setStatus(QuotaRequestStatus.APPROVED);
        quotaRequestRepository.save(testRequest);

        UUID requestId = testRequest.getRequestId();

        mockMvc.perform(patch("/ministry/statusApprove/" + requestId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Based on the existing controller logic finding "APPROVED" instead of matching string and returning 404
                .andExpect(content().string("Error: Request is already processed (APPROVED)"));
    }
}
