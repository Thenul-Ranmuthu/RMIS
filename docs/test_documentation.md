# Quota Request Ministry Officer Test Documentation

This document describes the test codes that were added to verify the `Approve` and `Reject` Quota Request functionality for the Ministry Officer.

## Overview
The tests are split into two levels:
1. **Unit Tests** (`MinistryOfficerServiceImplTest.java`): Tests the core business logic, state management, and interaction with dependencies (repositories, audit log service).
2. **Integration Tests** (`MinistryOfficerIntegrationTest.java`): Tests the complete REST API endpoints, security configurations, and full database persistence using Spring MVC Test and an in-memory test database structure.

---

## 1. Unit Tests (`MinistryOfficerServiceImplTest`)
This file uses Mockito to mock out the database layers (`QuotaRequestRepository`, `CompanyRepository`, etc.) and the `AuditLogService`. It directly ensures the business rules specified in the Acceptance Criteria are mathematically enforced without touching a real database.

### Test Scenarios Covered:
* `testApproveRequest_Success()`:
  - Validates **Scenario 1**.
  - Checks if a `PENDING` request correctly transitions to `APPROVED`.
  - Ensures the `reviewedBy` and `reviewedAt` fields are stamped accurately.
  - Asserts that the requested quota is perfectly deducted from the Company's overall available quota balance.
  - Verifies that `auditLogService.logApproval()` is called with correct arguments.

* `testRejectRequest_Success()`:
  - Validates **Scenario 2**.
  - Checks if a `PENDING` request transitions to `REJECTED`.
  - Ensures that the Company's quota balance **remains untouched**.
  - Ensures rejection remarks are correctly captured in the entity via `setRejectionReason()`.
  - Verifies that `auditLogService.logRejection()` is invoked.

* `testApproveRequest_StateEnforcement_Failure()` & `testRejectRequest_StateEnforcement_Failure()`:
  - Validates **Scenario 3 (State Enforcement)**.
  - Ensures that if a Request is *already* `APPROVED` or `REJECTED`, the system immediately returns an error.
  - Verifies that no database save or audit logging happens during an invalid state transition.

---

## 2. Integration Tests (`MinistryOfficerIntegrationTest`)
This file uses `@SpringBootTest` and `@AutoConfigureMockMvc` to spin up the entire Spring Core and emulate HTTP request lifecycles.

### Test Scenarios Covered:
* `setup()` & `teardown()`:
  - Safely seeds the test database with a `COMPANY` role, `MINISTRY_OFFICER` role, a mock Company, a mock Officer, and a fresh `PENDING` quota request.
  
* `shouldApprovePendingQuotaRequest()`:
  - Fires an HTTP `PATCH /ministry/statusApprove/{id}` request acting as an authorized Ministry Officer (`@WithMockUser`).
  - Expects a `200 OK` HTTP status response.
  - Reads data back from the real database to ensure the state reflects `APPROVED`.
  - Confirms the active company's quota decreased natively in the database.

* `shouldRejectPendingQuotaRequest()`:
  - Fires an HTTP `PATCH /ministry/statusReject/{id}` request with a rejection reason text body.
  - Expects a `200 OK` HTTP status response.
  - Queries the database to confirm the request became `REJECTED` and the remark text was saved.

* `shouldFailToApproveAlreadyProcessedRequest()`:
  - Changes the status of the entity manually inside the DB to `APPROVED`.
  - Sends the `PATCH` request.
  - Expects the controller to reject it with a non-OK status and return the `"Request is already processed"` exception string.
