# Design Notes

## Architecture Overview (frontend, backend, database)

STMS follows the standard AEM Cloud Service layered architecture:

```text
┌─────────────────────────────────────────────────────────────┐
│  Presentation (ui.apps + ui.content)                      │
│  HTL components, Granite dialogs, clientlibs, sample pages  │
├─────────────────────────────────────────────────────────────┤
│  Application (core)                                         │
│  Sling Models, OSGi TicketRepository, Sling Servlets        │
├─────────────────────────────────────────────────────────────┤
│  Infrastructure (ui.config)                                 │
│  Repoinit, service users, Oak index, logging                │
├─────────────────────────────────────────────────────────────┤
│  Data (JCR / Oak)                                           │
│  /content/stms/tickets — ticket + comment nodes             │
└─────────────────────────────────────────────────────────────┘
```

**Request flow (write):** HTL form → POST servlet → `TicketRepository` → service-user JCR session → redirect.  
**Request flow (read):** Page request → Sling Model → `TicketRepository` / resource adaptation → HTL render.

Supplementary detail: `data-model.md`, `ui-flow.md`

---

## Frontend Design

### Technology

- **HTL** for all component markup (`ui.apps/.../components/`)
- **Granite/Coral 3** dialogs (`_cq_dialog/.content.xml`)
- **Component clientlibs** for ticket-specific JS/CSS
- **Shared tokens** in `clientlib-base` (`tokens.css`, `badges.css`, `forms.css`, `alerts.css`)
- **App shell** (`appshell`) wraps ticket pages with sidebar + top bar

### Components

| Component | Resource type | Role |
|---|---|---|
| `appshell` | `stms/components/appshell` | Layout chrome, nav |
| `ticketlist` | `stms/components/ticketlist` | List, filters, sort |
| `ticketcreate` | `stms/components/ticketcreate` | Create form |
| `ticketdetail` | `stms/components/ticketdetail` | Read single ticket |
| `ticketedit` | `stms/components/ticketedit` | Update form |
| `ticketcomments` | `stms/components/ticketcomments` | Comment thread + form |

### UX conventions

- Status badges: `open`, `progress`, `resolved`, `closed`
- Priority indicators: `high`, `medium`, `low`
- Flash messages via query params: `created`, `updated`, `commentAdded`, `error`
- Forms POST to `/bin/stms/ticket/*` with `:cq_csrf_token`

### Pages (ui.content)

| Page | Path |
|---|---|
| List | `/content/stms/us/en/tickets` |
| Create | `/content/stms/us/en/tickets/create-ticket` |
| Edit | `/content/stms/us/en/tickets/edit-ticket` |
| Detail | `/content/stms/us/en/ticket-detail` |

---

## Backend Design

### Module: `core`

| Layer | Classes |
|---|---|
| **Repository** | `TicketRepository`, `TicketRepositoryImpl` |
| **DTOs** | `TicketCreateRequest`, `TicketUpdateRequest`, `TicketCommentCreateRequest`, `TicketSearchCriteria`, `*Result` |
| **Models** | `TicketModel`, `TicketListModel`, `TicketDetailModel`, `TicketCreateModel`, `TicketEditModel`, `TicketCommentsModel`, `AppShellModel` |
| **Servlets** | `TicketCreateServlet`, `TicketEditServlet`, `TicketCommentServlet` |
| **Enums** | `TicketStatus`, `TicketPriority` |

### Patterns

- **Repository pattern** — all JCR writes centralized in `TicketRepositoryImpl`
- **Thin servlets** — parse params, delegate, redirect (no JCR in servlet)
- **Service user writes** — subservice `stms-ticket-write`; never `loginAdministrative`
- **OSGi DS R6** — `@Component`, `@Reference` / `@OSGiService`
- **QueryBuilder** — list/search in repository; predicates on `sling:resourceType`, filters

### Security

- AEM author session for authentication
- CSRF on forms
- POST-only write endpoints (GET → 405)
- Server-side validation in repository (length, enums)

---

## Database Design

STMS uses **JCR (Oak)** as the data store — no external RDBMS.

### Root path

`/content/stms/tickets` — `sling:Folder` / `stms/tickets/folder`

### Ticket node

```
/content/stms/tickets/TICKET-0001
  sling:resourceType = stms/tickets/ticket
  ticketId, title, description, status, priority, assignee, createdDate
  jcr:createdBy (system)
  comments/
```

### Comment nodes

```
/content/stms/tickets/TICKET-0001/comments/comment-{timestamp}-{seq}
  sling:resourceType = stms/tickets/comment
  commentId, author, text, createdDate
```

### Indexing

Oak Lucene index `stms-ticket-index` on: `sling:resourceType`, `status`, `assignee`, `createdDate`

### ID generation

- Tickets: `TICKET-NNNN` (scan siblings)
- Comments: `comment-yyyyMMdd-HHmmss-NNN`

Full schema: `data-model.md`

---

## Validation Strategy

| Layer | Responsibility |
|---|---|
| **Client (JS)** | Required fields on create form before submit |
| **Servlet** | Parse parameters; no business rules |
| **Repository** | Title required (≤200), description required, valid enums, comment ≤5000 |
| **JCR** | `nt:unstructured` nodes; types enforced in Java only |

---

## Error Handling Strategy

| Failure | User experience |
|---|---|
| Validation error | Redirect to form with `?error=` and preserved fields |
| Missing folder / service user | Redirect with specific error message |
| Not found on update | Error message; no partial write |
| Success | Redirect to detail with flash param |

No JSON error bodies — HTML form flow only.

---

## Testing Strategy Link

See `test-strategy.md` for unit, integration, E2E, and manual smoke coverage.

Key test classes: `TicketRepositoryImpl*Test`, `*ServletTest`, `TicketCreateIT`.

---

## Key design decisions

| ID | Decision | Rationale |
|---|---|---|
| DD-1 | JCR nodes, not Content Fragments | Simpler CRUD for operational tickets |
| DD-2 | Form POST + redirect, not JSON API | AEM authoring + CSRF patterns |
| DD-3 | Service user for writes | Least privilege; Cloud Service best practice |
| DD-4 | Comments as child nodes | Audit trail; Sling Model iteration |

Design plan artifact: `ai-prompts/ticket_jcr_schema_design_24dcc4d5.plan.md`
