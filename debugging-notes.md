# Debugging Notes

Chronological record of real issues during STMS development on AEM Cloud SDK (local author `localhost:4502`). Each entry follows the assessment template.

---

## Issue 1 — Ticket create: "Ticket service is not available"

### Problem

Submitting the create-ticket form redirected to:

```text
/content/stms/us/en/tickets/create-ticket.html?error=Ticket+service+is+not+available.
```

Author `error.log`:

```text
ERROR [com.ttn.stms.core.tickets.services.impl.TicketRepositoryImpl]
  Unable to obtain ticket-write service resource resolver
org.apache.sling.api.resource.LoginException: Cannot get default resource resolver
```

### How I Investigated

1. Reproduced on create form POST
2. Checked OSGi console — `stms.core` was **Active**
3. Read `TicketRepositoryImpl.getTicketWriteResolver()` — uses subservice `stms-ticket-write`
4. Verified `ui.config` package install status — **not deployed** after adding mapping
5. Used Cursor with AEM MCP to confirm LoginException in logs

### How AI Helped

**Prompt:**

```text
Ticket create fails with "Ticket service is not available." on local author.
Check TicketRepositoryImpl getTicketWriteResolver, ui.config service user mapping,
and repoinit ACL for /content/stms/tickets. Use AEM MCP logs if needed.
```

AI pointed to `ServiceUserMapperImpl.amended~stms-tickets.cfg.json` and repoinit script. I verified files existed in source but had not been installed to the instance.

### What I Validated

- Deployed `ui.config` with `-PautoInstallPackage`
- Confirmed mapping: `stms.core:stms-ticket-write=[stms-ticket-service]`
- Re-tested create form — success redirect to detail page
- `mvn test -pl core -Dtest=TicketRepositoryImplCreateTest` still passed

### Final Fix

```bash
mvn clean install -pl ui.config -PautoInstallPackage
```

Files: `ui.config/.../ServiceUserMapperImpl.amended~stms-tickets.cfg.json`, `RepositoryInitializer~stms.cfg.json`

---

## Issue 2 — "Tickets folder is not configured"

### Problem

After deploying only `core` bundle, create form returned `error=Tickets+folder+is+not+configured.`

### How I Investigated

1. Traced error string to `TicketRepositoryImpl.createTicket()` — `ticketsRoot == null`
2. Opened CRX DE — `/content/stms/tickets` did not exist
3. Confirmed `ui.content` package not installed

### How AI Helped

Asked Cursor to map error message to code path. Response correctly identified `TICKETS_ROOT` check before node creation.

### What I Validated

- Installed `ui.content` package
- Folder appeared with sample `TICKET-0001`…`0003` nodes
- Create generated `TICKET-0004`

### Final Fix

```bash
mvn clean install -pl ui.content -PautoInstallPackage
```

---

## Issue 3 — Ticket list empty despite nodes in CRX

### Problem

List page showed no tickets; CRX had nodes under `/content/stms/tickets`.

### How I Investigated

1. Inspected `TicketRepositoryImpl` QueryBuilder predicates — requires `sling:resourceType=stms/tickets/ticket`
2. Found manually created test node missing `sling:resourceType`
3. Compared with sample content in `ui.content/.../tickets/.content.xml` — correct type present

### How AI Helped

Prompt asked to compare QueryBuilder predicates with sample content structure. AI highlighted `property.value=stms/tickets/ticket` requirement.

### What I Validated

- Sample tickets from content package appeared in list
- `mvn test -pl core -Dtest=TicketRepositoryImplTest` passed
- New tickets created via servlet had correct resource type

### Final Fix

No code change — data issue. Ensured `TicketRepositoryImpl.createTicket()` always sets resource type (already implemented). Re-created bad test nodes.

---

## Issue 4 — Unit test failure after comment validation

### Problem

```text
mvn test -pl core
TicketRepositoryImplAddCommentTest.addComment_rejectsBlankText — FAILED
```

### How I Investigated

Compared expected message in test vs `validateCommentRequest()` in `TicketRepositoryImpl`.

### How AI Helped

Pasted Maven failure output; AI aligned assertion with exact string `Comment text is required.`

### What I Validated

Full `mvn test -pl core` green after test fix.

### Final Fix

Updated test assertion in `TicketRepositoryImplAddCommentTest.java` — no production code change.

---

## Issue 5 — GET on create servlet returned 405

### Problem

Bookmarking `/bin/stms/ticket/create` showed "POST required".

### How I Investigated

Read `TicketCreateServlet.doGet()` — intentional 405.

### How AI Helped

Confirmed this is by design; suggested adding servlet unit test for GET → 405.

### What I Validated

Added assertion in `TicketCreateServletTest`.

### Final Fix

Test coverage only; no behavior change.

---

## Quick reference

| Error | First check |
|---|---|
| Ticket service is not available | Deploy `ui.config`; service-user mapping |
| Tickets folder is not configured | Deploy `ui.content` |
| Empty list | `sling:resourceType` on nodes |
| POST required | Use form POST, not GET URL |

**Related:** `ai-prompts/history/debugging.md`, `test-strategy.md`
