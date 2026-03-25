package com.rmis.rmis;

import com.rmis.rmis.controllers.QuotaRequestsController;
import com.rmis.rmis.domain.dtos.PagedResponseDto;
import com.rmis.rmis.domain.dtos.QuotaRequestResponseDto;
import com.rmis.rmis.domain.enums.QuotaRequestStatus;
import com.rmis.rmis.services.impl.ApplicationMinistryOfficerDetailsService;
import com.rmis.rmis.services.interfaces.QuotaRequestService;
import com.rmis.rmis.utils.JwtAuthenticationEntryPoint;
import com.rmis.rmis.utils.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuotaRequestsController.class)
public class QuotaRequestsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuotaRequestService quotaRequestService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean(name = "applicationCompanyDetailsService")
    private UserDetailsService companyDetailsService;

    @MockBean(name = "applicationPublicUserDetailsService")
    private UserDetailsService publicUserDetailsService;

    @MockBean(name = "applicationTechnicianDetailsService")
    private UserDetailsService technicianDetailsService;

    @MockBean
    private ApplicationMinistryOfficerDetailsService applicationMinistryOfficerDetailsService;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

    private void mockUser(String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "officer@test.com",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetAllRequests_Authorized() throws Exception {
        mockUser("MINISTRY_OFFICER");

        QuotaRequestResponseDto dto = QuotaRequestResponseDto.builder()
                .requestId("REQ-0001")
                .companyName("Test")
                .status(QuotaRequestStatus.PENDING)
                .build();

        Mockito.when(quotaRequestService.getAllRequests()).thenReturn(List.of(dto));

        mockMvc.perform(get("/ministry/quota-requests")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].request_id").value("REQ-0001"));
    }

    @Test
    void testGetAllRequests_UnauthorizedRole() throws Exception {
        mockUser("PUBLIC");

        mockMvc.perform(get("/ministry/quota-requests"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetPaginated_Success() throws Exception {
        mockUser("MINISTRY_OFFICER");

        PagedResponseDto<QuotaRequestResponseDto> pagedData = PagedResponseDto.<QuotaRequestResponseDto>builder()
                .data(List.of())
                .totalRecords(0)
                .currentPage(1)
                .build();

        Mockito.when(quotaRequestService.getQuotaRequestsPaginated(1, 10)).thenReturn(pagedData);

        mockMvc.perform(get("/ministry/quota-requests/paginated")
                .param("page", "1")
                .param("limit", "10"))
                .andExpect(status().isOk());
    }
}
