# AI Prompts — Testing

Reusable prompts for **test authoring and validation** on the STMS AEM Cloud Service project.

**Tool:** Cursor Agent mode  
**Read first:** `test-strategy.md`, `acceptance-criteria.md`

---

## When to use

- Adding unit tests after feature work
- Fixing failing `mvn test -pl core`
- Scaffolding integration or Cypress tests
- Mapping acceptance criteria to test cases

---

## Context to attach

| Artifact | Why |
|---|---|
| `test-strategy.md` | Pyramid, frameworks, coverage map |
| `acceptance-criteria.md` | Given/When/Then targets |
| Reference test | e.g. `@TicketRepositoryImplCreateTest.java` |
| Class under test | `@TicketRepositoryImpl.java` |

---

## Prompt 1 — Unit test for repository method

```text
Add JUnit 5 unit tests for [method] in TicketRepositoryImpl.

Follow TicketRepositoryImplCreateTest / TicketRepositoryImplUpdateTest patterns:
- AemContext with nodes under /content/stms/tickets
- sling:resourceType stms/tickets/ticket (and comments if needed)
- Assert JCR properties and *Result success/failure messages

Cover: happy path, validation failure, not found (if applicable).
Run: mvn test -pl core -Dtest=[TestClassName]
```

---

## Prompt 2 — Unit test for Sling Model

```text
Add unit tests for [ModelClass] in core.

Use io.wcm.testing.aem-mock.junit5 AemContext.
Adapt resource or request with correct sling:resourceType stms/components/[name] or stms/tickets/[type].

Assert HTL-exposed getters and edge cases (empty list, missing ticketId).
Match style of TicketListModelTest / TicketDetailModelTest.
```

---

## Prompt 3 — Servlet test

```text
Add tests for [ServletClass] (STMS ticket servlet).

Verify:
- POST with valid params → redirect URL contains expected query string
- POST with invalid params → redirect with error param
- GET returns 405

Use patterns from TicketCreateServletTest. Mock or wire TicketRepository as existing tests do.
```

---

## Prompt 4 — Fix failing tests

```text
mvn test -pl core failed with:

[paste failure output]

Fix the implementation or tests with minimal change. Do not disable tests.
Re-run mvn test -pl core and confirm all pass.
```

---

## Prompt 5 — Acceptance criteria → test map

```text
Map acceptance criteria [AC-x.x] from acceptance-criteria.md to:

1. Unit test class/method (existing or new)
2. Manual smoke step
3. Future E2E scenario ID (from test-strategy.md)

Output a table. Implement missing unit tests for gaps marked High priority.
```

---

## Prompt 6 — Enum / validation tests

```text
Add tests for TicketStatus / TicketPriority fromValue() and any new validation in TicketRepositoryImpl.

Cover unknown values, null, and boundary lengths (title 200, comment 5000).
Follow TicketStatusTest style.
```

---

## Prompt 7 — Integration test scaffold (it.tests)

```text
Scaffold an integration test in it.tests for STMS:

Scenario: POST /bin/stms/ticket/create on local author
Assert: 302 redirect and ticket node exists

Use AEM Testing Clients patterns from it.tests module. Document prerequisites (package deployed, tickets folder exists).
```

---

## Prompt 8 — Cypress E2E scaffold (ui.tests)

```text
Draft a Cypress test in ui.tests/test-module for STMS flow:

[E2E-1 create ticket | E2E-2 filter list | E2E-3 comment | E2E-4 edit]

Base URL: http://localhost:4502
Paths from ui-flow.md. Use data-testid hooks if missing — suggest minimal HTL additions only if necessary.
```

---

## Prompt 9 — Pre-merge test gate

```text
Run full STMS test gate and report:

1. mvn test -pl core
2. mvn clean install (note analyser failures only)
3. If dispatcher changed: cd dispatcher && ./bin/validate.sh src

Summarize pass/fail. Fix blocking failures only — no scope creep.
```

---

## Test commands (reference)

```bash
mvn test -pl core
mvn test -pl core -Dtest=TicketRepositoryImplCreateTest
mvn clean install
cd dispatcher && ./bin/validate.sh src
```

---

## Testing checklist

- [ ] New repository logic has `TicketRepositoryImpl*Test`
- [ ] New servlet has `*ServletTest`
- [ ] New model has `*ModelTest`
- [ ] Validation error messages match `api-contract.md`
- [ ] `mvn test -pl core` passes
- [ ] Acceptance criteria ID noted in test JavaDoc or commit message

---

## Related prompts

- **Debugging:** `ai-prompts/debugging.md` (when tests fail at runtime)
- **Implementation:** `ai-prompts/implementation.md`
