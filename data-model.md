# Data Model — STMS

JCR content structure, properties, and Sling Model mapping for the Support Ticket Management System.

---

## 1. Repository overview

| Item | Value |
|---|---|
| **Tickets root** | `/content/stms/tickets` |
| **Root type** | `sling:OrderedFolder` (or folder created via content package) |
| **Write ACL** | `stms-ticket-service` — `jcr:read`, `rep:write`, `jcr:versionManagement`, `jcr:lockManagement` |
| **Search index** | `/_oak_index/stms-ticket-index` (Lucene) |

---

## 2. Node hierarchy

```text
/content/stms/tickets/                          [folder]
└── TICKET-0001/                                nt:unstructured
    ├── jcr:primaryType                         nt:unstructured
    ├── sling:resourceType                      stms/tickets/ticket
    ├── ticketId                                "TICKET-0001"
    ├── title                                   "Cannot access DAM assets"
    ├── description                             "User reports 403 on /assets.html"
    ├── status                                  "open" | "in-progress" | "resolved" | "closed"
    ├── priority                                "low" | "medium" | "high" | "critical"
    ├── assignee                                "agent@example.com" (optional)
    ├── createdDate                               Calendar
    ├── jcr:createdBy                             (system — creator username)
    └── comments/                                 nt:unstructured
        ├── sling:resourceType                    stms/tickets/comments
        └── comment-20260901-143022-001/          nt:unstructured
            ├── sling:resourceType                stms/tickets/comment
            ├── commentId                         "comment-20260901-143022-001"
            ├── author                            "author@example.com"
            ├── text                              "Investigating permissions."
            └── createdDate                       Calendar
```

---

## 3. Entity definitions

### 3.1 Ticket (`stms/tickets/ticket`)

| Property | JCR type | Required | Description |
|---|---|---|---|
| `jcr:primaryType` | Name | Yes | `nt:unstructured` |
| `sling:resourceType` | String | Yes | `stms/tickets/ticket` |
| `ticketId` | String | Yes | Duplicate of node name (`TICKET-NNNN`) |
| `title` | String | Yes | Max 200 characters |
| `description` | String | Yes | Free text |
| `status` | String | Yes | See `TicketStatus` enum |
| `priority` | String | Yes | See `TicketPriority` enum |
| `assignee` | String | No | Assignee email or identifier |
| `createdDate` | Date | Yes | Set on create |
| `jcr:createdBy` | String | System | AEM user who created the node |

**Sling Model:** `TicketModel` (`adaptables = Resource.class`)

**ID generation:** `TicketRepositoryImpl.generateNextTicketId()` — scans siblings matching `TICKET-\d+`, increments.

---

### 3.2 Comments container (`stms/tickets/comments`)

| Property | JCR type | Required | Description |
|---|---|---|---|
| `jcr:primaryType` | Name | Yes | `nt:unstructured` |
| `sling:resourceType` | String | Yes | `stms/tickets/comments` |

**Sling Model:** `TicketCommentsContainerModel`

Created automatically with each new ticket. Lazily created on first comment if missing.

---

### 3.3 Comment (`stms/tickets/comment`)

| Property | JCR type | Required | Description |
|---|---|---|---|
| `jcr:primaryType` | Name | Yes | `nt:unstructured` |
| `sling:resourceType` | String | Yes | `stms/tickets/comment` |
| `commentId` | String | Yes | Node name (`comment-{timestamp}-{seq}`) |
| `author` | String | Yes | Resolved from logged-in user |
| `text` | String | Yes | Max 5000 characters |
| `createdDate` | Date | Yes | Set on create |

**Sling Model:** `TicketCommentModel` (`adaptables = Resource.class`)

**ID generation:** `generateCommentId()` — `comment-yyyyMMdd-HHmmss-NNN`.

---

## 4. Enumerations

### TicketStatus

| Stored value | Label |
|---|---|
| `open` | Open |
| `in-progress` | In Progress |
| `resolved` | Resolved |
| `closed` | Closed |

**Default on create:** `open`

### TicketPriority

| Stored value | Label |
|---|---|
| `low` | Low |
| `medium` | Medium |
| `high` | High |
| `critical` | Critical |

**Default on create form:** `medium` (UI default in `TicketCreateModel`)

---

## 5. OSGi service layer

### TicketRepository

```text
TicketRepository (interface)
    ├── getTicket(resolver, ticketId)           → Optional<TicketModel>
    ├── findTickets(resolver, criteria)         → List<TicketModel>
    ├── findAllTickets(resolver)                → List<TicketModel>
    ├── createTicket(request)                   → TicketCreateResult
    ├── updateTicket(request)                   → TicketUpdateResult
    └── addComment(request)                     → TicketCommentCreateResult
```

**Implementation:** `TicketRepositoryImpl`

**Write subservice:** `stms-ticket-write`

---

## 6. DTOs (transfer objects)

| Class | Fields | Used by |
|---|---|---|
| `TicketCreateRequest` | title, description, priority, assignee | `createTicket()` |
| `TicketUpdateRequest` | ticketId, title, description, status, priority, assignee | `updateTicket()` |
| `TicketCommentCreateRequest` | ticketId, text, author | `addComment()` |
| `TicketSearchCriteria` | status, assignee, priority, creator, sortAscending, limit | `findTickets()` |

---

## 7. Query model

`TicketRepositoryImpl` builds QueryBuilder predicates:

| Predicate | Property | Notes |
|---|---|---|
| `path` | — | `/content/stms/tickets` |
| `type` | — | `nt:unstructured` |
| `property` | `sling:resourceType` | `stms/tickets/ticket` |
| `property` | `status` | When filter set |
| `property` | `assignee` | When filter set |
| `property` | `priority` | When filter set |
| `property` | `jcr:createdBy` | When creator filter set |
| `orderby` | `@createdDate` | `desc` or `asc` |
| `p.limit` | — | From criteria (default unlimited) |

**Index:** `stms-ticket-index` indexes `sling:resourceType`, `status`, `assignee`, `createdDate`.

---

## 8. Component ↔ content mapping

| AEM component | `sling:resourceType` | Data source |
|---|---|---|
| `ticketlist` | `stms/components/ticketlist` | `TicketRepository.findTickets()` |
| `ticketdetail` | `stms/components/ticketdetail` | `TicketRepository.getTicket()` via `ticketId` param |
| `ticketcreate` | `stms/components/ticketcreate` | Form only (no JCR read) |
| `ticketedit` | `stms/components/ticketedit` | `TicketRepository.getTicket()` |
| `ticketcomments` | `stms/components/ticketcomments` | Comments from parent `TicketModel` |
| `appshell` | `stms/components/appshell` | Nav config + optional ticket counts |

---

## 9. Configuration data (ui.config)

### Repoinit (`RepositoryInitializer~stms.cfg.json`)

- Creates `/content/dam/stms` with `cq:conf=/conf/stms`
- Creates service user `stms-ticket-service`
- Grants ACL on `/content/stms/tickets`

### Service user mapping

```json
"stms.core:stms-ticket-write=[stms-ticket-service]"
```

---

## 10. Sample content paths (ui.content)

| Page | Path |
|---|---|
| Ticket list | `/content/stms/us/en/tickets` |
| Create ticket | `/content/stms/us/en/tickets/create-ticket` |
| Edit ticket | `/content/stms/us/en/tickets/edit-ticket` |
| Ticket detail | `/content/stms/us/en/ticket-detail` |

Detail and edit pages resolve ticket via `?ticketId=TICKET-NNNN`.

---

## 11. Entity relationship diagram

```text
┌─────────────────┐       1     *      ┌──────────────────────┐
│     Ticket      │───────────────────►│  CommentsContainer   │
│  TICKET-NNNN    │                    │      comments/       │
└────────┬────────┘                    └──────────┬───────────┘
         │                                        │ 1
         │ properties                             │
         │ title, status, ...                     │ *
         │                                        ▼
         │                             ┌──────────────────────┐
         │                             │       Comment        │
         │                             │ comment-{ts}-{seq}   │
         └────────────────────────────►│ author, text, date   │
              (logical parent)         └──────────────────────┘
```

---

## 12. Related documents

- `api-contract.md` — Servlet write contracts
- `design-notes.md` — Architecture decisions
- `acceptance-criteria.md` — Validation rules as test criteria
