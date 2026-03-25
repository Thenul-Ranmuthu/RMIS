package com.rmis.rmis.services.interfaces;

import java.util.UUID;

public interface MinistryOfficerService {

    String changeQuotaRequestStatusApprove(UUID id);

    String changeQuotaRequestStatusReject(UUID id, String reason);

}
