# Prompt History — Design

---

## P-DES-001 | JCR schema (Plan mode)

**Date:** 2026-08-31  
**Mode:** Plan

### Prompt

```text
Design a concrete JCR node schema under /content/stms/tickets for a Support Ticket
Management System on AEM Cloud SDK. Each ticket node should store: id, title,
description, status, priority, assignee, creation date, and comments as child nodes.
Provide ASCII directory structure and OSGi-friendly Sling Model architecture outline.
```

### AI response

Created plan file with todos, ASCII tree, design principles (data nodes not pages, node name = ticket ID), model class outline.

### Accepted

- `nt:unstructured` ticket nodes under `/content/stms/tickets`
- Comments as child nodes under `comments/` container
- `TicketRepository` for listing via QueryBuilder
- Resource types: `stms/tickets/ticket`, `stms/tickets/comment`

### Changed

- Split `TicketListModel` to use `SlingHttpServletRequest` (not `Resource`) for request-param filters
- Added Oak index todo after first list performance concern

### Rejected

- AI option to store comments as JSON property on ticket — chose child nodes for auditability and Sling Model iteration

**Artifact:** `ai-prompts/ticket_jcr_schema_design_24dcc4d5.plan.md`

---

## P-DES-002 | Servlet vs JSON API

**Date:** 2026-08-31  
**Mode:** Ask

### Prompt

```text
Should STMS ticket writes use JSON REST servlets or form POST for AEM author UI?
```

### AI response

Recommended form POST + redirect for HTL forms, CSRF, and AEM authoring patterns; defer JSON API.

### Accepted

- Form POST + 302 redirect pattern
- Thin servlets delegating to repository

### Changed

- Documented in `design-notes.md` as DD-2 / form POST decision

### Rejected

- Headless JSON API for MVP — out of scope

---

## P-DES-003 | Service user design

**Date:** 2026-08-31  
**Mode:** Agent

### Prompt

```text
Configure repoinit and service user mapping for ticket writes under /content/stms/tickets.
Use subservice stms-ticket-write in TicketRepositoryImpl.
```

### AI response

Generated `RepositoryInitializer~stms.cfg.json` and `ServiceUserMapperImpl.amended~stms-tickets.cfg.json`.

### Accepted

- `stms-ticket-service` user
- ACL on `/content/stms/tickets` only
- Mapping `stms.core:stms-ticket-write=[stms-ticket-service]`

### Changed

- Verified subservice constant matches Java `TICKET_WRITE_SUBSERVICE`

### Rejected

- Using request user's resolver for writes — security requirement
