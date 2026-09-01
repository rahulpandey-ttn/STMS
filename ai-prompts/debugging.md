# Prompt History — Debugging

---

## P-DBG-001 | AEM log review

**Date:** 2026-08-30  
**Mode:** Agent + MCP

### Prompt

```text
Review the local AEM author log tail and summarize any active OSGi warnings.
```

### AI response

Summarized WARN/ERROR lines; flagged bundle state and missing config packages.

### Accepted

- Use MCP `logs` tool for author instance
- Follow-up to deploy `ui.config`

### Changed

- Documented in `debugging-notes.md` Issue 1

### Rejected

- Ignoring warnings as "normal" — investigated `stms.core` and config packages

---

## P-DBG-002 | Service user failure

**Date:** 2026-08-31  
**Mode:** Agent + MCP

### Prompt

```text
Ticket create fails with "Ticket service is not available." on local author.
Check TicketRepositoryImpl getTicketWriteResolver, ui.config service user mapping,
and repoinit ACL for /content/stms/tickets. Use AEM MCP logs if needed.
```

### AI response

Traced to `LoginException` in `getTicketWriteResolver()`; listed missing `ui.config` deploy.

### Accepted

- Root cause: service-user mapping not installed
- Fix: `mvn install -pl ui.config -PautoInstallPackage`

### Changed

- Added verification steps to debugging notes

### Rejected

- AI suggestion to use admin session in servlet as quick fix — security violation

---

## P-DBG-003 | Empty ticket list

**Date:** 2026-09-01  
**Mode:** Agent

### Prompt

```text
Ticket list is empty but /content/stms/tickets has nodes in CRX.
Review TicketRepositoryImpl findTickets predicates and TicketListModel.
```

### AI response

Identified missing `sling:resourceType=stms/tickets/ticket` on manual test nodes.

### Accepted

- QueryBuilder property predicate explanation

### Changed

- Verified sample content nodes have correct resource type

### Rejected

- Disabling resourceType predicate — would return wrong node types

---

## P-DBG-004 | Maven compile failure (IT)

**Date:** 2026-09-01  
**Mode:** Agent

### Prompt

```text
TicketCreateIT fails to compile: getHeader not found, doDelete wrong signature.
Fix using AEM Testing Clients API. Re-run mvn -pl it.tests compile.
```

### AI response

Use `getFirstHeader(HttpHeaders.LOCATION)`; simplify `doDelete` call.

### Accepted

- API fixes per SlingHttpResponse / SlingClient

### Changed

- —

### Rejected

- —
