package com.rmis.rmis.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmis.rmis.domain.entities.Company;
import com.rmis.rmis.domain.entities.Role;
import com.rmis.rmis.repositories.CompanyRepository;
import com.rmis.rmis.repositories.QuotaRequestRepository;
import com.rmis.rmis.repositories.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Quota Request feature.
 *
 * These tests start the full Spring Boot application context and simulate
 * real HTTP requests to the /quotaHeader/addQuota endpoint using MockMvc.
 *
 * The @Transactional annotation ensures every test is rolled back after
 * completion, keeping the real PostgreSQL database clean.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Rollback
public class QuotaRequestIntegrationTest {

    // ── Constants ──────────────────────────────────────────────────────────
    private static final String ENDPOINT = "/quotaHeader/addQuota";
    private static final String COMPANY_EMAIL = "integration.test@company.com";
    private static final String COMPANY_ID = "INTEG-TEST-001";
    private static final String ROLE_NAME = "COMPANY";

    // ── Injected Beans ─────────────────────────────────────────────────────
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private QuotaRequestRepository quotaRequestRepository;

    // Instantiated directly — ObjectMapper is not a Spring bean in RANDOM_PORT mode
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    // ── Test Data + MockMvc Setup ──────────────────────────────────────────

    @BeforeEach
    void setUp() {
        // Build MockMvc with Spring Security support
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // 1. Get or create the COMPANY role
        Role role = roleRepository.findByName(ROLE_NAME);
        if (role == null) {
            role = new Role();
            role.setName(ROLE_NAME);
            role = roleRepository.save(role);
        }

        // 2. Create a test company (@PrePersist sets quota=0 and status=PENDING)
        Company company = new Company();
        company.setName("Integration Test Co");
        company.setEmail(COMPANY_EMAIL);
        company.setCompanyid(COMPANY_ID);
        company.setPassword("test-password-hash");
        company.setRole(role);
        company = companyRepository.save(company);

        // 3. Manually update quota to 1000 (after PrePersist has run)
        company.setQuota(new BigDecimal("1000.00"));
        companyRepository.save(company);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 1 — Successful Quota Request
    // ══════════════════════════════════════════════════════════════════════

    /**
     * GIVEN: An authenticated company with 1000 quota.
     * WHEN: They request 500 (within balance).
     * THEN: 200 OK is returned with a success message.
     */
    @Test
    @WithMockUser(username = COMPANY_EMAIL)
    void test1_SuccessfulQuotaRequest_Returns200() throws Exception {
        Map<String, Object> payload = Map.of("requestedQuota", 500);

        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("saved")));
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 2 — Insufficient Quota
    // ══════════════════════════════════════════════════════════════════════

    /**
     * GIVEN: An authenticated company with 1000 quota.
     * WHEN: They request 99999 (exceeds balance).
     * THEN: 400 Bad Request is returned.
     */
    @Test
    @WithMockUser(username = COMPANY_EMAIL)
    void test2_InsufficientQuota_Returns400() throws Exception {
        Map<String, Object> payload = Map.of("requestedQuota", 99999);

        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 3 — Unauthenticated Access
    // ══════════════════════════════════════════════════════════════════════

    /**
     * GIVEN: No JWT token (anonymous user).
     * WHEN: They call the addQuota endpoint.
     * THEN: 401 Unauthorized is returned.
     */
    @Test
    void test3_UnauthenticatedRequest_Returns401() throws Exception {
        Map<String, Object> payload = Map.of("requestedQuota", 100);

        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 4 — Company Not Found
    // ══════════════════════════════════════════════════════════════════════

    /**
     * GIVEN: A token is valid but email does not match any company in DB.
     * WHEN: addQuota is called.
     * THEN: 500 Internal Server Error is returned (RuntimeException from service).
     */
    @Test
    @WithMockUser(username = "ghost@unknown.com")
    void test4_CompanyNotFound_Returns500() throws Exception {
        Map<String, Object> payload = Map.of("requestedQuota", 100);

        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isInternalServerError());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 5 — Boundary: Exactly Equal to Available Quota
    // ══════════════════════════════════════════════════════════════════════

    /**
     * GIVEN: An authenticated company with exactly 1000 quota.
     * WHEN: They request exactly 1000 (boundary value).
     * THEN: 200 OK is returned (equal-to is allowed by the >= check).
     */
    @Test
    @WithMockUser(username = COMPANY_EMAIL)
    void test5_ExactQuotaBoundary_Returns200() throws Exception {
        Map<String, Object> payload = Map.of("requestedQuota", 1000);

        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 6 — Zero Quota Request
    // ══════════════════════════════════════════════════════════════════════

    /**
     * GIVEN: An authenticated company.
     * WHEN: They request 0 quota.
     * THEN: 400 Bad Request is returned (assuming we want to prevent 0 requests).
     */
    @Test
    @WithMockUser(username = COMPANY_EMAIL)
    void test6_ZeroQuotaRequest_Returns400() throws Exception {
        Map<String, Object> payload = Map.of("requestedQuota", 0);

        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    // ══════════════════════════════════════════════════════════════════════
    // TEST 7 — Negative Quota Request
    // ══════════════════════════════════════════════════════════════════════

    /**
     * GIVEN: An authenticated company.
     * WHEN: They request -100 quota.
     * THEN: 400 Bad Request is returned.
     */
    @Test
    @WithMockUser(username = COMPANY_EMAIL)
    void test7_NegativeQuotaRequest_Returns400() throws Exception {
        Map<String, Object> payload = Map.of("requestedQuota", -100);

        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }
}
