# Test Strategy — STMS

Testing approach, coverage map, and execution guide for the Support Ticket Management System.

---

## 1. Testing pyramid

```text
                    ┌─────────────┐
                    │  UI E2E     │  ui.tests (Cypress) — planned / pipeline
                    │  (few)      │
                ┌───┴─────────────┴───┐
                │  Integration      │  it.tests — Cloud Manager step
                │  (some)           │
            ┌───┴───────────────────┴───┐
            │  Unit tests (many)          │  core — JUnit 5 + AEM Mock
            └─────────────────────────────┘
```

| Layer | Module | Runner | When |
|---|---|---|---|
| **Unit** | `core` | `mvn test -pl core` | Every change to Java logic |
| **Integration** | `it.tests` | Cloud Manager / manual against AEM | API and content flows |
| **UI E2E** | `ui.tests` | Cypress against running AEM | Critical user journeys |
| **Build** | all | `mvn clean install` | AEM analyser, package validation |
| **Dispatcher** | `dispatcher` | `./bin/validate.sh src` | Dispatcher config changes |
| **Manual smoke** | local SDK | Browser on author | Post-deploy verification |

---

## 2. Unit tests (core)

### Framework stack

| Library | Purpose |
|---|---|
| JUnit 5 | Test runner |
| WCM.io AEM Mock (`io.wcm.testing.aem-mock.junit5`) | Sling/AEM context |
| Mockito | Mocking collaborators |
| `AemContext` | In-memory resource tree |

### Coverage map

| Area | Test classes | What is verified |
|---|---|---|
| **Repository — read** | `TicketRepositoryImplTest` | `getTicket`, `findTickets`, QueryBuilder predicates |
| **Repository — create** | `TicketRepositoryImplCreateTest` | Node creation, properties, comments child, ID generation |
| **Repository — update** | `TicketRepositoryImplUpdateTest` | Property updates, not-found handling |
| **Repository — comment** | `TicketRepositoryImplAddCommentTest` | Comment node creation, container lazy-create |
| **Models** | `TicketModelTest`, `TicketListModelTest`, `TicketDetailModelTest`, `TicketCommentsModelTest` | Adaptation, getters, comment sorting, badge classes |
| **Shell** | `AppShellModelTest` | Nav items, page path resolution |
| **Servlets** | `TicketCreateServletTest`, `TicketEditServletTest`, `TicketCommentServletTest` | POST handling, redirect URLs, error paths |
| **Enums** | `TicketStatusTest` | `fromValue()` mapping |
| **Scaffolding** | `HelloWorldModelTest`, `LoggingFilterTest`, etc. | Archetype boilerplate |

### Run commands

```bash
# All core tests
mvn test -pl core

# Single test class
mvn test -pl core -Dtest=TicketRepositoryImplCreateTest

# With coverage (if jacoco configured)
mvn test -pl core
```

### Unit test conventions

1. **Arrange** ticket nodes under `/content/stms/tickets` in `AemContext`
2. **Set** `sling:resourceType` to match production values
3. **Inject** `TicketRepositoryImpl` with context resolver factory for write tests
4. **Assert** JCR properties and result DTOs (`TicketCreateResult.isSuccess()`, etc.)

### Required coverage for changes

| Change type | Minimum test update |
|---|---|
| New repository method | New test method or class in `TicketRepositoryImpl*Test` |
| New validation rule | Test success + failure message |
| New servlet parameter | Servlet test for redirect URL |
| New model property | Model test for getter / HTL-exposed value |
| Enum value added | `TicketStatusTest` or `TicketPriority` equivalent |

---

## 3. Integration tests (it.tests)

### Purpose

Validate HTTP-level behavior against a **running** AEM author/publish instance using AEM Testing Clients.

### Scope (recommended)

| Test | Endpoint / page | Assertion |
|---|---|---|
| Create ticket POST | `/bin/stms/ticket/create` | 302 redirect; node exists in JCR |
| Update ticket POST | `/bin/stms/ticket/update` | Properties updated |
| Add comment POST | `/bin/stms/ticket/comment` | Comment node created |
| List page | `/content/stms/us/en/tickets.html` | 200 OK |
| Detail page | `/content/stms/us/en/ticket-detail.html?ticketId=...` | 200 OK with ticket title |

### Execution

- Cloud Manager **Custom Functional Testing** step in full stack pipeline
- Local: deploy package, then run `it.tests` module against `localhost:4502`

---

## 4. UI tests (ui.tests)

### Framework

- **Cypress** in `ui.tests/test-module/`
- Cloud Manager **Custom UI Testing** step

### Recommended E2E scenarios

| ID | Scenario | Steps |
|---|---|---|
| E2E-1 | Create ticket | Navigate to create → fill form → submit → see detail |
| E2E-2 | List filter | Open list → filter by status → verify row count |
| E2E-3 | Add comment | Open detail → submit comment → see in thread |
| E2E-4 | Edit ticket | Open edit → change status → save → verify on detail |
| E2E-5 | App shell nav | Click sidebar links → correct pages load |

### Execution (local)

```bash
# Requires AEM running with STMS content deployed
cd ui.tests/test-module
npm install
npm test
```

---

## 5. Manual smoke test checklist

Run after `mvn clean install -PautoInstallSinglePackage` on local author.

| # | Step | Expected |
|---|---|---|
| 1 | Open `/content/stms/us/en/tickets.html` | List renders; shell visible |
| 2 | Click Create ticket | Form loads |
| 3 | Submit valid ticket | Redirect to detail; `TICKET-*` in URL |
| 4 | CRX DE: `/content/stms/tickets` | New node with correct properties |
| 5 | Add comment on detail | Comment in thread after redirect |
| 6 | Edit ticket status | Detail shows updated status |
| 7 | Filter list by status | Filtered results |
| 8 | OSGi console: `stms.core` | Bundle Active |

---

## 6. AI-assisted testing workflow

| Step | AI role | Human/agent action |
|---|---|---|
| 1 | Draft unit test from existing pattern | "Add test for X like `TicketRepositoryImplCreateTest`" |
| 2 | Run `mvn test -pl core` | Agent or developer executes |
| 3 | Fix failures | Feed stack trace to Cursor Agent |
| 4 | MCP diagnostics | `user-aem-local-author` logs if runtime failure |
| 5 | Review diff | Ensure tests assert behavior, not implementation details |

See `.res.local/documents/tool-workflow.md` for full AI workflow.

---

## 7. Validation gates (CI / pre-merge)

| Gate | Command | Blocking |
|---|---|---|
| Unit tests | `mvn test -pl core` | Yes |
| Full build | `mvn clean install` | Yes |
| AEM analyser | Part of `mvn install` | Yes |
| Dispatcher validate | `cd dispatcher && ./bin/validate.sh src` | Yes (if dispatcher changed) |
| Integration tests | Cloud Manager pipeline | Yes (in pipeline) |
| UI tests | Cloud Manager pipeline | Yes (in pipeline) |

---

## 8. Test data management

| Approach | Detail |
|---|---|
| **Unit tests** | Self-contained `AemContext` resources; no external AEM |
| **Integration / E2E** | Use dedicated test tickets (`TICKET-TEST-*`) or cleanup in teardown |
| **Local manual** | Sample content in `ui.content`; safe to create tickets on author |
| **Production** | Never run destructive tests against production |

---

## 9. Defect classification

| Severity | Example | Response |
|---|---|---|
| **Critical** | Cannot create tickets; bundle inactive | Block release; fix + regression test |
| **High** | Validation bypass; wrong data persisted | Fix before merge |
| **Medium** | UI styling issue; filter edge case | Fix in sprint |
| **Low** | Copy/text issues | Backlog |

---

## 10. Traceability matrix

| Acceptance ID | Unit test | Integration | E2E | Manual |
|---|---|---|---|---|
| AC-1.x | `TicketRepositoryImplCreateTest` | Create POST | E2E-1 | Smoke #3–4 |
| AC-2.x | `TicketListModelTest` | List page GET | E2E-2 | Smoke #1, #7 |
| AC-3.x | `TicketDetailModelTest` | Detail GET | E2E-1 | Smoke #3 |
| AC-4.x | `TicketCreateServletTest` | Create POST | E2E-1 | Smoke #2–3 |
| AC-5.x | `TicketEditServletTest`, `UpdateTest` | Update POST | E2E-4 | Smoke #6 |
| AC-6.x | `AddCommentTest`, `CommentServletTest` | Comment POST | E2E-3 | Smoke #5 |
| AC-7.x | `AppShellModelTest` | — | E2E-5 | Smoke #1 |
| AC-8.x | Write resolver tests | Service user | — | Smoke #8 |
| AC-9.x | Full `mvn test` | Pipeline | Pipeline | Build |

---

## 11. Related documents

- `acceptance-criteria.md` — Given/When/Then criteria
- `api-contract.md` — Servlet contracts for integration tests
- `implementation-plan.md` — Phase 5 hardening tasks
- `.res.local/documents/tool-workflow.md` — AI validation workflow
