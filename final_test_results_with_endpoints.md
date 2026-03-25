# Quota Management System - Comprehensive Test Results

This document provides a detailed breakdown of the testing scenarios for the Quota Management feature, including the specific API endpoints, expected vs. actual outcomes, and identified bugs.

---

### 🟢 Scenario 1: Successful Submission
**Objective:** Verify that a valid quota request is processed and stored correctly.

| API Endpoint | AC Point | Expected | Actual | Result |
| :--- | :--- | :--- | :--- | :--- |
| `POST /quotaHeader/addQuota` | Save quota amount | 500 | 500.00 | ✅ PASS |
| `POST /quotaHeader/addQuota` | Save justification | Text saved in DB | Column does not exist — NULL | ❌ BUG-01 |
| `POST /quotaHeader/addQuota` | Auto-record date | Today's date | 2026-03-24 14:37:22 | ✅ PASS |
| `POST /quotaHeader/addQuota` | Set status | PENDING | PENDING | ✅ PASS |
| `POST /quotaHeader/addQuota` | Notification | New row in notification table | ERROR: relation "notifications" does not exist | ❌ BUG-02 |
| `POST /quotaHeader/addQuota` | Success Message | Success message | "Quota saved succefully!!" (spelling error) | ⚠️ PARTIAL |

---

### 🟡 Scenario 2: Validation Rules
**Objective:** Ensure the system prevents invalid data from being saved.

| API Endpoint | AC Point | Expected | Actual | Result |
| :--- | :--- | :--- | :--- | :--- |
| `POST /quotaHeader/addQuota` | Quota > 0 | 400 Bad Request | Submission succeeds with zero/negative value | ❌ BUG-04 |
| `POST /quotaHeader/addQuota` | Justification required | 400 Bad Request | Justification field does not exist in logic | ❌ BUG-05 |
| `POST /quotaHeader/addQuota` | Prevent duplicates | 400 Bad Request | Second request saved successfully | ❌ BUG-06 |

---

### 🔴 Scenario 3: Unauthorized Access
**Objective:** Verify that the security layer protects the endpoint from non-company users.

| API Endpoint | AC Point | Expected | Actual | Result |
| :--- | :--- | :--- | :--- | :--- |
| `POST /quotaHeader/addQuota` | Deny non-company | 403 Forbidden | 500 Internal Server Error — server crashes | ❌ BUG-07 |
| `POST /quotaHeader/addQuota` | Auth Error Message | "Access denied — company users only" | Generic server crash message — no proper error | ❌ BUG-08 |
