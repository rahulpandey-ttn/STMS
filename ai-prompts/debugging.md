# AI Prompts — Debugging

Reusable prompts for **troubleshooting STMS** on local AEM author and in code.

**Tool:** Cursor Agent mode + MCP `user-aem-local-author` (when AEM is running)  
**Read first:** `api-contract.md`, `data-model.md`, `design-notes.md`

---

## When to use

- Servlet returns 500 or unexpected redirect
- Bundle `stms.core` not active
- Tickets not saving or list empty
- QueryBuilder / index issues
- Repoinit or service-user ACL failures

---

## Context to attach

| Source | When |
|---|---|
| Maven stack trace | Compile or test failures |
| Browser URL + behavior | UI/servlet issues |
| MCP `logs` | Runtime exceptions |
| MCP `diagnose-osgi-bundle` | OSGi / DS issues |
| MCP `recent-requests` | Failed HTTP requests |
| `@TicketRepositoryImpl.java` | Write path bugs |

---

## Prompt 1 — Servlet / form failure

```text
STMS ticket form fails:

Page: [create | edit | comment]
URL: [full URL with query params]
Expected: [redirect to detail with ticketId]
Actual: [error message or status]

Trace:
1. TicketCreateServlet / TicketEditServlet / TicketCommentServlet
2. TicketRepositoryImpl validation and persistence
3. ui.config service user mapping and repoinit ACL on /content/stms/tickets

Use AEM MCP recent-requests and logs if available.
Propose minimal fix and verify steps.
```

---

## Prompt 2 — OSGi bundle not active

```text
stms.core bundle is not Active on local AEM author.

Use MCP diagnose-osgi-bundle for com.ttn.stms.core (or stms.core artifact).

Report:
- Bundle state
- Unsatisfied DS references
- Recent ERROR log lines for com.ttn.stms

Suggest fix (missing config, import package, service user mapping).
```

---

## Prompt 3 — Ticket list empty

```text
STMS ticket list shows no tickets but nodes exist under /content/stms/tickets in CRX.

Check:
1. TicketListModel and TicketSearchCriteria / URL filters
2. TicketRepositoryImpl QueryBuilder predicates (sling:resourceType=stms/tickets/ticket)
3. Oak index stms-ticket-index in ui.config
4. Node properties vs TicketModel expectations

Diagnose and fix with minimal change. Add unit test if query bug found.
```

---

## Prompt 4 — Write permission / service user

```text
Ticket create fails with "Ticket service is not available" or persistence error.

Verify STMS write chain:
1. TICKET_WRITE_SUBSERVICE = stms-ticket-write in TicketRepositoryImpl
2. ui.config ServiceUserMapperImpl.amended~stms-tickets.cfg.json
3. repoinit: stms-ticket-service ACL on /content/stms/tickets
4. Package ui.config deployed

Use MCP logs for LoginException or AccessDeniedException.
```

---

## Prompt 5 — Maven build failure

```text
STMS Maven build failed:

[paste mvn output]

Fix compilation, test, or analyser errors. Minimal diff only.
Re-run: mvn test -pl core or mvn clean install -pl [module] as appropriate.
```

---

## Prompt 6 — HTL / component not rendering

```text
STMS component [ticketlist | ticketdetail | appshell | ...] does not render correctly on author.

Check:
- sling:resourceType on page node matches component
- Sling Model adapts (resourceType registration)
- HTL data-sly-use and property names
- clientlibs css.txt/js.txt and categories

Compare with working ticketcreate component. Fix HTL or dialog config.
```

---

## Prompt 7 — AEM log review (MCP)

```text
Review local AEM author logs for STMS-related issues.

Use MCP user-aem-local-author logs tool.
Filter for: com.ttn.stms, RepositoryInitializer, stms-ticket-service, ERROR, WARN

Summarize active warnings and errors with recommended actions. No code changes unless asked.
```

**STMS reference:** Prompt #1 in `STMS-propmts-history.md`.

---

## Prompt 8 — CSRF or 403 on POST

```text
POST to /bin/stms/ticket/create returns 403 or CSRF error.

Check HTL form for :cq_csrf_token
Verify Granite CSRF config on author
Compare ticketcreate.html with working AEM form patterns

Fix form markup only unless servlet issue proven.
```

---

## Debug decision tree

```text
Symptom?
├─ Build fails → Maven output → Prompt 5
├─ Bundle inactive → MCP diagnose-osgi-bundle → Prompt 2
├─ HTTP 500 on POST → MCP recent-requests + logs → Prompt 1
├─ Empty list → QueryBuilder + index → Prompt 3
├─ Save fails silently → Service user chain → Prompt 4
└─ UI wrong → HTL + Model → Prompt 6
```

---

## Local verification URLs

| Page | URL |
|---|---|
| Ticket list | `http://localhost:4502/content/stms/us/en/tickets.html` |
| Create | `http://localhost:4502/content/stms/us/en/tickets/create-ticket.html` |
| Detail | `http://localhost:4502/content/stms/us/en/ticket-detail.html?ticketId=TICKET-0001` |
| OSGi | `http://localhost:4502/system/console/bundles` (filter: stms) |

---

## Debugging checklist

- [ ] `stms.core` bundle Active
- [ ] `ui.config` package installed (repoinit applied)
- [ ] `/content/stms/tickets` exists
- [ ] MCP logs checked for stack trace
- [ ] Reproduced with single ticket minimal case
- [ ] Fix verified with `mvn test -pl core` + manual smoke

---

## Related prompts

- **Testing:** `ai-prompts/testing.md`
- **Code review:** `ai-prompts/code-review.md`
