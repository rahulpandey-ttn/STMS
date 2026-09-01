# Design Notes — STMS

Architecture and design decisions for the Support Ticket Management System.

---

## 1. Architecture overview

```text
┌─────────────────────────────────────────────────────────────────┐
│  AEM Author (localhost:4502)                                    │
│                                                                 │
│  ┌──────────────┐    HTL + clientlibs    ┌──────────────────┐ │
│  │ ui.apps      │ ◄────────────────────── │ Pages (ui.content)│ │
│  │ components   │                         └──────────────────┘ │
│  └──────┬───────┘                                               │
│         │ Sling Models adapt                                    │
│         ▼                                                       │
│  ┌──────────────┐    OSGi @OSGiService   ┌──────────────────┐  │
│  │ core         │ ◄───────────────────── │ TicketRepository │  │
│  │ Sling Models │                        │ (impl)           │  │
│  └──────┬───────┘                        └────────┬─────────┘  │
│         │                                         │             │
│         │ POST forms                              │ service user│
│         ▼                                         ▼             │
│  ┌──────────────┐                        ┌──────────────────┐  │
│  │ Servlets     │ ──────────────────────►│ JCR Repository │  │
│  │ /bin/stms/*  │                        │ /content/stms/ │  │
│  └──────────────┘                        │ tickets        │  │
│                                           └──────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Key design decisions

### DD-1 — JCR as ticket store (not external DB)

**Decision:** Persist tickets as `nt:unstructured` nodes under `/content/stms/tickets`.

**Rationale:**
- Native AEM content model; no additional infrastructure
- QueryBuilder + Oak index for listing/filtering
- Replication-ready if publish views are added later

**Trade-off:** Not ideal for very high-volume ticketing; acceptable for author-side support use case.

---

### DD-2 — Repository pattern with service-user writes

**Decision:** All mutations go through `TicketRepository` using subservice `stms-ticket-write`.

**Rationale:**
- Centralizes validation and ID generation
- Servlet layer stays thin (parse params → delegate → redirect)
- Service user avoids elevating the end-user's session for writes

**Implementation:**
- `stms-ticket-service` created via repoinit
- Mapping: `stms.core:stms-ticket-write=[stms-ticket-service]`

---

### DD-3 — Form POST + redirect (not JSON REST)

**Decision:** Write endpoints accept `application/x-www-form-urlencoded` and respond with HTTP redirects.

**Rationale:**
- Matches AEM authoring patterns and CSRF token handling
- Simple HTL form integration without SPA framework
- Error state preserved via query parameters

**Trade-off:** Not headless-friendly; a JSON API can be added later as a separate layer.

---

### DD-4 — Sling Models split by adaptables

| Model | Adaptable | Reason |
|---|---|---|
| `TicketModel`, `TicketCommentModel` | `Resource` | Content-structure nodes; used in list iteration |
| `TicketListModel`, `TicketCreateModel`, etc. | `SlingHttpServletRequest` | Need request params, page context, OSGi services |

---

### DD-5 — Comments as child nodes (not JSON array property)

**Decision:** Each comment is a child node under `comments/` with `sling:resourceType=stms/tickets/comment`.

**Rationale:**
- Aligns with Sling Model `@PostConstruct` child iteration in `TicketModel`
- Supports per-comment metadata (author, date) without parsing JSON
- Comment IDs: `comment-yyyyMMdd-HHmmss-NNN`

---

### DD-6 — Ticket ID as node name

**Decision:** Node name = `ticketId` (e.g. `TICKET-0001`).

**Rationale:**
- Human-readable URLs and repository paths
- Sequential ID generation scans sibling node names
- `ticketId` property duplicates node name for QueryBuilder indexing

---

### DD-7 — Component-scoped clientlibs

**Decision:** Ticket components ship dedicated `clientlibs/` for JS/CSS; shared tokens in `clientlib-base`.

**Rationale:**
- Avoids loading ticket JS on non-ticket pages
- Design tokens (`tokens.css`, `badges.css`, `forms.css`) reused across components

---

### DD-8 — App shell as layout wrapper

**Decision:** `stms/components/appshell` wraps ticket page content with sidebar + top bar.

**Rationale:**
- Single place for navigation and branding
- Dialog-configurable paths (`ticketsListPage`, `createTicketPage`)
- `AppShellModel` integrates `TicketRepository` for nav badge counts (if configured)

---

## 3. Module responsibilities

| Module | Responsibility |
|---|---|
| `core` | OSGi services, Sling Models, servlets, enums, unit tests |
| `ui.apps` | HTL, Granite dialogs, component clientlibs, i18n |
| `ui.config` | Repoinit, service-user mapping, Oak index, logging, CORS |
| `ui.content` | Editable templates, policies, sample ticket pages |
| `ui.frontend` | Webpack site bundle → `clientlib-site` |
| `dispatcher` | Cache, filters, vhosts (default archetype) |

---

## 4. Security model

| Layer | Mechanism |
|---|---|
| **Authentication** | AEM author login (Sling authentication) |
| **Authorization (writes)** | Service user ACL on `/content/stms/tickets` |
| **CSRF** | Granite CSRF token (`:cq_csrf_token`) on forms |
| **Input validation** | Server-side in `TicketRepositoryImpl`; client-side in component JS |
| **Servlet methods** | POST only for write endpoints; GET returns 405 |

---

## 5. Search and indexing

- **Query API:** AEM QueryBuilder via `TicketRepositoryImpl.findTickets`
- **Index:** `stms-ticket-index` (Lucene, async) on `sling:resourceType`, `status`, `assignee`, `createdDate`
- **Default sort:** `createdDate` descending (newest first)
- **Filters:** status, assignee, priority, creator (via `jcr:createdBy` predicate)

---

## 6. UI / UX conventions

| Element | Convention |
|---|---|
| Status badge CSS | `open`, `progress`, `resolved`, `closed` (from `TicketModel.getStatusBadgeClass()`) |
| Priority indicator | `high`, `medium`, `low` (from `getPriorityLevelClass()`) |
| Assignee avatar | Initials + color variant 1–4 from email hash |
| Success flash | Query params: `created=true`, `updated=true`, `commentAdded=true` |
| Error flash | `error` query param with URL-encoded message |

---

## 7. Cloud Service compliance

- No writes to `/libs`
- No `Session.loginAdministrative`
- OSGi DS R6 annotations (`@Component`, `@Reference`)
- Deploy via Cloud Manager pipeline
- AEM SDK API pinned in root `pom.xml`
- Java 21 (`.cloudmanager/java-version`)

---

## 8. Alternatives considered

| Alternative | Why not chosen (baseline) |
|---|---|
| Content Fragments for tickets | More suited to structured headless content; JCR nodes simpler for CRUD |
| SPA (React) for ticket UI | Adds complexity; HTL sufficient for author tool |
| Sling POST Servlet JSON API | Deferred; form POST meets current UI needs |
| Granite Workflow on create | Out of scope; available via `aem-workflow` skill for future |

---

## 9. Related documents

- `data-model.md` — JCR schema detail
- `api-contract.md` — Servlet contracts
- `ui-flow.md` — User journeys
- `implementation-plan.md` — Phased delivery
