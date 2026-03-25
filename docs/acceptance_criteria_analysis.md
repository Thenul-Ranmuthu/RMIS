# Acceptance Criteria Analysis — Approve / Reject Quota Request

## Summary

| Scenario | Status |
|---|---|
| Scenario 1 – Approve Request | ⚠️ Partially Met |
| Scenario 2 – Reject Request | ⚠️ Partially Met |
| Scenario 3 – State Enforcement | ❌ Not Met |

---

## Scenario 1 – Approve Request

| Criterion | Code Location | Status |
|---|---|---|
| Change status to `APPROVED` | `MinistryOfficerServiceImpl.java` L32 | ✅ Done |
| Record approval timestamp (`reviewed_at`) | `QuotaRequest` entity has `reviewedAt` field, but **never set** in `changeQuotaRequestStatusApprove()` | ❌ Missing |
| Record officer ID (`reviewedBy`) | Entity has `reviewedBy` field (FK to `MinistryOfficer`), but **never set** | ❌ Missing |
| Update company's approved quota balance | `MinistryOfficerServiceImpl` L39: `company.setQuota(quota.subtract(requested))` | ✅ Done |
| Recalculate remaining quota | Same subtract operation covers this | ✅ Done |
| Generate notification to company | No `sendNotification`/email call anywhere in approval flow | ❌ Missing |
| Create an audit log entry | `AuditLogServiceImpl.logApproval()` exists but is **never called** from `MinistryOfficerServiceImpl` | ❌ Missing |

> **Backend score: 3 / 7**

**Frontend (Scenario 1):**
- `ReviewModal.tsx` has Approve button that calls `approveRequest()` → `PATCH /ministry/statusApprove/{id}` ✅
- However `approveRequest()` in `quotaService.ts` (L214–227) **swallows HTTP errors** — it returns the raw text even on a 4xx/5xx, so the UI will never show an error to the user ⚠️
- `QuotaReviewModal.tsx` (the detail modal opened via `onReview`) only shows _details_, no Approve/Reject buttons at all ❌ — the two modal implementations are inconsistent

---

## Scenario 2 – Reject Request

| Criterion | Code Location | Status |
|---|---|---|
| Change status to `REJECTED` | `MinistryOfficerServiceImpl.java` L55 | ✅ Done |
| Record rejection timestamp | `reviewedAt` **never set** | ❌ Missing |
| Record officer ID | `reviewedBy` **never set** | ❌ Missing |
| Store rejection remarks in DB | `QuotaRequest` entity has **no `rejectionReason`/`remarks` field** at all | ❌ Missing |
| Generate notification to company | Not implemented | ❌ Missing |
| Create audit log entry | `AuditLogServiceImpl.logRejection()` exists but is **never called** | ❌ Missing |
| Confirm before rejecting (UI) | `ReviewModal` has no confirmation step or remarks input before rejection | ❌ Missing |

> **Backend score: 1 / 6**

**Frontend (Scenario 2):**
- Reject button calls `rejectRequest()` ✅
- No confirmation dialog before rejection ❌
- No rejection remarks/reason text input ❌
- `rejectRequest()` passes `authHeaders(token)` but `authHeaders` is defined twice in the file with different signatures, so this call likely fails silently ❌

---

## Scenario 3 – State Enforcement

| Criterion | Code Location | Status |
|---|---|---|
| Prevent re-processing APPROVED/REJECTED requests | `changeQuotaRequestStatusApprove()` and `changeQuotaRequestStatusReject()` only check existence, **not current status** | ❌ Missing |
| Return appropriate error message | No status check → no error message | ❌ Missing |
| Ensure status cannot be modified after decision | Same gap — the service will happily overwrite any status | ❌ Missing |

> **Backend score: 0 / 3**

**Frontend (Scenario 3):**
- `QuotaTable` shows a different label ("Details" / "View Log") per status and `ReviewModal` is only opened for `PENDING` rows ✅
- But if a direct API call is made (or `QuotaReviewModal` which has no action buttons), there is no server-side guard ❌

---

## Code Quality Issues Found

1. **`quotaService.ts` is duplicated** — the file contains two complete implementations merged together (RMIS-23 and RMIS-27 branches). Both `authHeaders`, `getQuotaRequests`, and constants are defined twice. This will cause a compile error or unpredictable runtime behaviour.

2. **`quota-requests/page.tsx` is similarly merged** — the page has two `export default` statements and duplicate component code (RMIS-23 and RMIS-27). Only one version can be the active one; the other is dead code.

3. **`QuotaTable.tsx` is also duplicated** — contains RMIS-23 and RMIS-27 versions interleaved.

4. **`MinistryOfficerController` string comparison bug**: Approve endpoint checks `"Status set to ACCEPTED"` (L27) but the service returns `"Status set to APPROVED"` (L43) — so successful approvals always return `404 NOT_FOUND`.

---

## Priority Fix List

### 🔴 Critical (blocks functionality)

1. **Fix string mismatch** in `MinistryOfficerController` — change `"Status set to ACCEPTED"` → `"Status set to APPROVED"`.

2. **Add state guard** in both service methods:
   ```java
   if (quotaRequest.getStatus() != QuotaRequestStatus.PENDING) {
       return "Error: Request is already " + quotaRequest.getStatus();
   }
   ```

3. **Clean up duplicated frontend files** (`quotaService.ts`, `page.tsx`, `QuotaTable.tsx`) — keep only the RMIS-27 version.

### 🟠 High (acceptance criteria gaps)

4. **Set `reviewedAt` and `reviewedBy`** in `MinistryOfficerServiceImpl` (both approve and reject methods). Requires resolving the logged-in officer from `SecurityContextHolder`.

5. **Add rejection remarks** — add a `rejectionReason` column to `QuotaRequest` entity, pass it from the frontend, and set it in the reject method.

6. **Call `auditLogService.logApproval()`** and `logApproval.logRejection()` from `MinistryOfficerServiceImpl`.

7. **Add a confirmation + remarks dialog** before rejection in the frontend (`ReviewModal`).

### 🟡 Medium (quality / completeness)

8. **Fix `approveRequest()` in `quotaService.ts`** to properly throw on non-OK responses.

9. **Notify company by email** on approval and rejection (extend `EmailService` with `sendApprovalEmail` and `sendRejectionEmail`).
