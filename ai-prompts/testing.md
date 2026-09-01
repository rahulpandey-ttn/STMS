# Prompt History — Testing

---

## P-TST-001 | Repository unit tests

**Date:** 2026-08-31  
**Mode:** Agent  
**Context:** `@TicketRepositoryImplCreateTest.java`

### Prompt

```text
Add unit tests for TicketRepositoryImpl createTicket matching existing test style.
Cover success, validation failures, and tickets folder missing.
```

### AI response

`TicketRepositoryImplCreateTest` with AemContext ticket nodes.

### Accepted

- Test structure mirroring archetype `AppAemContext`
- Assertions on JCR properties after create

### Changed

- Added explicit assertion for `comments` child container

### Rejected

- Mockito-only test without AemContext — insufficient for JCR verification

---

## P-TST-002 | Servlet redirect tests

**Date:** 2026-09-01  
**Mode:** Agent

### Prompt

```text
Add TicketCreateServletTest for POST success redirect and GET 405.
```

### AI response

Mock `TicketRepository`, assert 302 Location header.

### Accepted

- GET → 405 test
- Redirect URL contains `ticketId` and `created=true`

### Changed

- None

### Rejected

- —

---

## P-TST-003 | Fix failing comment test

**Date:** 2026-09-01  
**Mode:** Agent

### Prompt

```text
TicketRepositoryImplAddCommentTest fails after validation change.
Fix assertions to match validateCommentRequest messages. Run mvn test -pl core.
```

### AI response

Updated expected error string to `Comment text is required.`

### Accepted

- Test fix only

### Changed

- —

### Rejected

- Weakening validation to match wrong test — fixed test instead

---

## P-TST-004 | Integration test scaffold

**Date:** 2026-09-01  
**Mode:** Agent

### Prompt

```text
Scaffold TicketCreateIT in it.tests using AEM Testing Clients.
POST /bin/stms/ticket/create, assert 302 and JCR node. Follow CreatePageIT patterns.
```

### AI response

`TicketCreateIT.java` with `FormEntityBuilder`, `CQAuthorClassRule`, cleanup in `@After`.

### Accepted

- IT class compiles with `aem-cloud-testing-clients` 1.3.0
- Documents prerequisites and `mvn verify -Plocal` command

### Changed

- Fixed `getFirstHeader(HttpHeaders.LOCATION)` after compile error
- Fixed `doDelete` signature

### Rejected

- Running IT in default `mvn test` — requires `local` profile and running AEM
