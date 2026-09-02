# Test Results

Recorded on **2026-09-01** against the STMS baseline on local workstation (macOS). Traceability: `test-strategy.md`, `acceptance-criteria.md`.

---

## Summary

| Suite | Command | Result | Tests | Notes |
|---|---|---|---|---|
| Core unit tests | `mvn test -pl core` | **PASS** | 44 / 44 | 18 test classes, 0 failures |
| Full reactor (partial) | `mvn install -rf :stms.ui.apps` | **PARTIAL** | — | `ui.apps` → `all` succeeded; `it.tests` failed at compile |
| AEM analyser | (via `stms.all` package) | **PASS** | — | 0 errors, 0 warnings on analysed features |
| Integration tests | `mvn verify -pl it.tests -Plocal` | **FAIL (compile)** | 0 run | `TicketCreateIT` does not compile |
| Dispatcher validation | `dispatcher/bin/validate.sh src` | **NOT RUN** | — | Script not present in repo (Dispatcher SDK not installed locally) |
| Cypress UI tests | `ui.tests` | **NOT RUN** | — | Scaffold only; no specs executed |
| Manual author smoke | Local AEM `localhost:4502` | **PASS** | 6 / 6 | Verified during development (see below) |
| OSGi runtime | AEM MCP `diagnose-osgi-bundle` | **PASS** | — | `stms.core` Active; 8 DS components Active |

**Overall:** Unit test gate is green. Integration test module requires a compile fix before execution. Manual flows verified on local author after package deploy.

---

## Environment

| Item | Value |
|---|---|
| Date | 2026-09-01 |
| AEM author | `localhost:4502` (Cloud SDK Quickstart) |
| Java | 21 (per `.cloudmanager/java-version`) |
| Maven | 3.x |
| Project version | `1.0.0-SNAPSHOT` |
| Packages deployed | `ui.apps`, `ui.config`, `ui.content`, `core` bundle (via `-PautoInstallSinglePackage` during development) |

---

## Core Unit Tests

**Command:**

```bash
mvn test -pl core
```

**Result:** `BUILD SUCCESS` — **44 tests, 0 failures, 0 errors, 0 skipped** (elapsed ~4.7 s)

### STMS ticket tests (14 classes, 37 tests)

| Test class | Tests | Failures | Status |
|---|---:|---:|---|
| `TicketRepositoryImplTest` | 4 | 0 | PASS |
| `TicketRepositoryImplCreateTest` | 3 | 0 | PASS |
| `TicketRepositoryImplUpdateTest` | 3 | 0 | PASS |
| `TicketRepositoryImplAddCommentTest` | 3 | 0 | PASS |
| `TicketModelTest` | 5 | 0 | PASS |
| `TicketListModelTest` | 6 | 0 | PASS |
| `TicketDetailModelTest` | 5 | 0 | PASS |
| `TicketCommentsModelTest` | 2 | 0 | PASS |
| `TicketCreateServletTest` | 1 | 0 | PASS |
| `TicketEditServletTest` | 1 | 0 | PASS |
| `TicketCommentServletTest` | 1 | 0 | PASS |
| `AppShellModelTest` | 2 | 0 | PASS |
| `TicketStatusTest` | 3 | 0 | PASS |

### Archetype baseline tests (4 classes, 7 tests)

| Test class | Tests | Failures | Status |
|---|---:|---:|---|
| `LoggingFilterTest` | 1 | 0 | PASS |
| `SimpleServletTest` | 1 | 0 | PASS |
| `HelloWorldModelTest` | 1 | 0 | PASS |
| `SimpleResourceListenerTest` | 1 | 0 | PASS |
| `SimpleScheduledTaskTest` | 1 | 0 | PASS |

### Edge cases covered (unit)

| Scenario | Test class | Status |
|---|---|---|
| Blank title on create | `TicketRepositoryImplCreateTest` | PASS |
| Invalid priority on create | `TicketRepositoryImplCreateTest` | PASS |
| Ticket not found on update | `TicketRepositoryImplUpdateTest` | PASS |
| Blank comment text | `TicketRepositoryImplAddCommentTest` | PASS |
| Comment length validation | `TicketRepositoryImplAddCommentTest` | PASS |
| Redirect URL on servlet success | `TicketCreateServletTest`, `TicketEditServletTest`, `TicketCommentServletTest` | PASS |
| List filters and sort criteria | `TicketListModelTest` | PASS |
| Missing ticket on detail | `TicketDetailModelTest` | PASS |

---

## Integration Tests (`it.tests`)

**Command attempted:**

```bash
mvn clean verify -pl it.tests -Plocal
```

**Result:** `BUILD FAILURE` — compilation error; **0 tests executed**

```
TicketCreateIT.java:[88,77] incompatible types: int cannot be converted to java.util.List<org.apache.http.NameValuePair>
```

| Test class | Status | Notes |
|---|---|---|
| `CreatePageIT` | NOT RUN | Blocked by compile failure in same module |
| `GetPageIT` | NOT RUN | Blocked by compile failure in same module |
| `TicketCreateIT` | COMPILE ERROR | `doDelete()` second argument uses `HttpStatus.SC_OK` (int) instead of expected `List<NameValuePair>` |

**Follow-up:** Fix `TicketCreateIT.cleanup()` signature, redeploy packages, re-run with AEM on port 4502.

---

## Full Build & AEM Analyser

**Commands:**

```bash
mvn clean install          # failed at ui.apps clean (target file lock)
mvn install -rf :stms.ui.apps   # resumed successfully through stms.all
```

| Module | Build | Tests |
|---|---|---|
| `core` | SUCCESS | 44 passed |
| `ui.frontend` | SUCCESS | — |
| `ui.apps.structure` | SUCCESS | — |
| `ui.apps` | SUCCESS | — |
| `ui.content` | SUCCESS | — |
| `ui.config` | SUCCESS | — |
| `stms.all` | SUCCESS | AEM analyser: **0 errors, 0 warnings** |
| `it.tests` | FAILURE | Compile error (see above) |
| `dispatcher` | SKIPPED | — |
| `ui.tests` | SKIPPED | — |

AEM analyser plugin version warning: project uses `1.6.6`; latest recommended `1.7.4` (non-blocking).

---

## Runtime Verification (Local Author)

Verified via AEM MCP on `localhost:4502`:

### OSGi bundle `stms.core`

| Property | Value |
|---|---|
| State | **ACTIVE** |
| Version | `1.0.0.SNAPSHOT` |

### Declarative Services components (all ACTIVE)

- `TicketRepositoryImpl`
- `TicketCreateServlet`
- `TicketEditServlet`
- `TicketCommentServlet`
- `LoggingFilter`
- `SimpleServlet`
- `SimpleScheduledTask`
- `SimpleResourceListener`

---

## Manual Smoke Checklist

Executed on local author after deploying `ui.apps`, `ui.config`, and `ui.content`. Documented in `debugging-notes.md`.

| # | Step | Result |
|---|---|---|
| 1 | Ticket list loads at `/content/stms/us/en/tickets.html` with app shell | PASS |
| 2 | Sample tickets from content package visible in list | PASS |
| 3 | Create ticket — redirect to detail with new `TICKET-*` | PASS |
| 4 | Add comment — appears in thread | PASS |
| 5 | Edit status — persists on detail | PASS |
| 6 | OSGi `stms.core` Active | PASS |

**Issues encountered and resolved during smoke (see `debugging-notes.md`):**

- "Ticket service is not available" — fixed by deploying `ui.config` (service user mapping)
- "Tickets folder is not configured" — fixed by deploying `ui.content`
- Empty list for manually created nodes — data issue (missing `sling:resourceType`)

---

## Acceptance Criteria — Testing Section

| Criterion | Status |
|---|---|
| `mvn test -pl core` passes (18+ test classes) | **PASS** (18 classes, 44 tests) |
| `mvn clean install` passes with AEM analyser | **PARTIAL** — analyser passes; full clean install hit file-lock on `ui.apps`; `it.tests` compile blocks reactor |
| `TicketCreateIT` compiles; passes against local author | **FAIL** — does not compile |
| Manual smoke: create → list → detail → edit → comment | **PASS** |
| `dispatcher/bin/validate.sh src` passes | **NOT RUN** — script absent locally |

---

## Known Gaps

| Gap | Impact | Planned follow-up |
|---|---|---|
| `TicketCreateIT` compile error | Integration gate blocked | Fix `doDelete` call signature |
| `TicketEditIT` / `TicketCommentIT` | No servlet IT coverage for update/comment | Add after create IT is stable |
| Cypress E2E (`ui.tests`) | No automated UI regression | Phase 6 spec |
| Dispatcher SDK validation | Not executed in this run | Install SDK and run `validate.sh` |
| Publish-tier flows | Out of MVP scope | Future phase |

---

## Commands Reference

```bash
# Unit tests (primary gate)
mvn test -pl core

# Integration tests (requires AEM + compile fix)
mvn clean verify -pl it.tests -Plocal

# Full build with analyser
mvn clean install

# Deploy to local author
mvn clean install -PautoInstallSinglePackage
```
