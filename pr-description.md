# PR Description

## Summary

Implements **STMS (Support Ticket Management System)** on AEM as a Cloud Service — a full-stack author-side ticketing app with JCR storage, HTL components, OSGi repository, Sling servlets, service-user writes, unit tests, integration test scaffold, and assessment documentation.

---

## Features Implemented

- Ticket **list** with sort and filter (status, assignee, priority, creator)
- Ticket **create**, **detail**, **edit**, and **comment** flows
- **App shell** with sidebar navigation and top bar
- **Service-user** persistence (`stms-ticket-service` / `stms-ticket-write`)
- Sample tickets and pages in `ui.content`
- Oak Lucene index for ticket queries
- Shared design tokens in `clientlib-base`

---

## Technical Changes

| Module | Changes |
|---|---|
| `core` | `TicketRepository`, models, servlets, enums, 18+ unit tests |
| `ui.apps` | 6 components (appshell, ticket*), HTL, dialogs, clientlibs |
| `ui.config` | Repoinit, service-user mapping, `stms-ticket-index` |
| `ui.content` | Ticket pages, sample data under `/content/stms/tickets` |
| `it.tests` | `TicketCreateIT` — POST create servlet integration test |
| Root docs | Assessment artifact set (`requirements-analysis.md`, etc.) |
| `ai-prompts/` | Prompt playbooks + `history/` by activity |

---

## Database Changes

STMS uses **JCR (Oak)** — no SQL schema.

| Path | Type |
|---|---|
| `/content/stms/tickets` | Folder root |
| `/content/stms/tickets/TICKET-NNNN` | `stms/tickets/ticket` |
| `.../comments/comment-*` | `stms/tickets/comment` |

Repoinit creates service user and ACL. See `data-model.md`.

---

## Testing Done

- [x] `mvn test -pl core` — all unit tests pass
- [x] `mvn clean install` — full build + AEM analyser
- [x] `mvn -pl it.tests compile` — `TicketCreateIT` compiles
- [ ] `mvn clean verify -pl it.tests -Plocal` — run against local AEM (requires deployed packages)
- [x] Manual smoke on author: list → create → detail → edit → comment

---

## AI Usage Summary

- **Cursor** (Agent, Ask, Plan) for design, implementation, debugging, documentation
- **AEM MCP** for log tail and OSGi diagnosis during service-user issue
- **`.agents/skills/`** as reference (`create-component`, not full auto-pilot)
- Prompt history with accept/reject: `ai-prompts/history/`
- Key plan artifact: `ai-prompts/ticket_jcr_schema_design_24dcc4d5.plan.md`

Human review applied to all Agent diffs; tests run before accepting Java changes.

---

## Screenshots / Demo Notes

### Local URLs (author)

| Step | URL |
|---|---|
| List | `http://localhost:4502/content/stms/us/en/tickets.html` |
| Create | `http://localhost:4502/content/stms/us/en/tickets/create-ticket.html` |
| Detail | `http://localhost:4502/content/stms/us/en/ticket-detail.html?ticketId=TICKET-0001` |

### Demo script (5 min)

1. Show ticket list with sample data and filters
2. Create new ticket → redirect to detail with `created=true`
3. Add comment → thread updates
4. Edit status to In Progress → save → detail reflects change
5. Show CRX node under `/content/stms/tickets`

### Deploy

```bash
mvn clean install -PautoInstallSinglePackage
```

---

## Known Limitations

- Author-tier only; no publish write flows
- Assignee is free-text email, not user picker
- No ticket delete/archive
- No email notifications or Granite Workflow
- Form POST + redirect only — no headless JSON API
- Cypress E2E not implemented (IT scaffold for create only)
- No `_cq_design_dialog` on components

---

## Future Improvements

- `TicketEditIT`, `TicketCommentIT`, Cypress E2E
- Assignee Granite user picker
- Headless JSON read API for tickets
- Granite Workflow for approval on close
- Publish read-only ticket views
- Ticket attachments via DAM references

**Related:** `implementation-plan.md` Phase 6, `reflection.md`
