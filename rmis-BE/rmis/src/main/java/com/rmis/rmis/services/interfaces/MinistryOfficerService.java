package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.entities.MinistryOfficer;

import java.util.UUID;

public interface MinistryOfficerService {

    String changeQuotaRequestStatusApprove(UUID id, MinistryOfficer officer);

    String changeQuotaRequestStatusReject(UUID id, MinistryOfficer officer);

}
