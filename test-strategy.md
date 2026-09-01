# Test Strategy

## Test Scope

In scope for STMS baseline:

- Ticket repository CRUD and validation (`TicketRepositoryImpl`)
- Sling Models (list, detail, create, edit, comments, app shell)
- Servlet redirect behavior for create, update, comment
- Integration: POST create servlet against running AEM author
- Manual author smoke for full UI flow

Out of scope (MVP):

- Publish-tier flows
- Cypress E2E (scaffold only in `ui.tests`)
- Load/performance testing
- Security penetration testing

---

## Unit Tests

**Module:** `core`  
**Framework:** JUnit 5, WCM.io AEM Mock, Mockito  
**Command:** `mvn test -pl core`

| Area | Test classes |
|---|---|
| Repository read | `TicketRepositoryImplTest` |
| Repository create | `TicketRepositoryImplCreateTest` |
| Repository update | `TicketRepositoryImplUpdateTest` |
| Repository comment | `TicketRepositoryImplAddCommentTest` |
| Models | `TicketModelTest`, `TicketListModelTest`, `TicketDetailModelTest`, `TicketCommentsModelTest` |
| Servlets | `TicketCreateServletTest`, `TicketEditServletTest`, `TicketCommentServletTest` |
| Shell | `AppShellModelTest` |
| Enums | `TicketStatusTest` |

**Conventions:**

- Arrange ticket nodes under `/content/stms/tickets` with correct `sling:resourceType`
- Assert validation messages match `api-contract.md`
- Add/update tests when repository or servlet behavior changes

---

## Component Tests

Component-level behavior is covered indirectly:

| Layer | Approach |
|---|---|
| HTL + Model binding | `*ModelTest` with `AemContext` |
| Client-side validation | Manual smoke + future Cypress |
| Dialog config | Manual author verification |

No separate HTL unit test framework — AEM Mock models are the component contract tests.

---

## API / Integration Tests

**Module:** `it.tests`  
**Framework:** JUnit 4, AEM Cloud Testing Clients, `CQAuthorClassRule`  
**Command:** `mvn clean verify -pl it.tests -Plocal` (requires AEM on `4502` + deployed packages)

| Test | What it verifies |
|---|---|
| `CreatePageIT` | Archetype baseline — page exists |
| `GetPageIT` | Archetype baseline — page GET |
| `TicketCreateIT` | POST `/bin/stms/ticket/create` → 302 + JCR node + comments child |

**Prerequisites for `TicketCreateIT`:**

```bash
mvn clean install -PautoInstallSinglePackage
```

---

## Edge Case Tests

| Edge case | Unit test coverage |
|---|---|
| Blank title on create | `TicketRepositoryImplCreateTest` |
| Invalid priority | `TicketRepositoryImplCreateTest` |
| Ticket not found on update | `TicketRepositoryImplUpdateTest` |
| Blank comment text | `TicketRepositoryImplAddCommentTest` |
| Comment length > 5000 | `TicketRepositoryImplAddCommentTest` |
| GET on servlet → 405 | `TicketCreateServletTest` |
| Redirect URL on success | `TicketCreateServletTest` |
| Empty list / wrong resourceType | `TicketRepositoryImplTest` (manual + query predicates) |

---

## Tests Not Covered (and why)

| Gap | Reason | Planned follow-up |
|---|---|---|
| Cypress E2E for full UI flow | Time; IT covers servlet layer | Phase 6 — `ui.tests` spec |
| `TicketEditIT` / `TicketCommentIT` | Scaffold priority was create path first | Add after `TicketCreateIT` stable |
| Publish-tier rendering | MVP is author-only | Future publish read views |
| Dispatcher cache for ticket pages | No publish traffic yet | When publish enabled |
| Concurrent ticket ID collision | Low risk on author; sequential IDs | Load test if scale needed |
| Granite Workflow steps | Out of MVP scope | `aem-workflow` skill phase |

---

## Manual smoke checklist

After deploy to local author:

1. Open ticket list — tickets visible
2. Filter by status — list updates
3. Create ticket — redirect to detail with new `TICKET-*`
4. Add comment — appears in thread
5. Edit status — persists on detail
6. OSGi: `stms.core` Active

---

## CI / pipeline

| Gate | When |
|---|---|
| `mvn test -pl core` | Every Java change |
| `mvn clean install` | Pre-merge |
| `it.tests` | Cloud Manager Custom Functional Testing |
| `ui.tests` | Cloud Manager Custom UI Testing (when specs added) |

Traceability: `acceptance-criteria.md` Testing section
