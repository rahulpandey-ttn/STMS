# Requirements Analysis — STMS

## Document control

| Item | Detail |
|---|---|
| **Project** | STMS (Support Ticket Management System) |
| **Platform** | AEM as a Cloud Service |
| **Status** | Implemented (baseline) |
| **Source prompts** | `.res.local/documents/STMS-propmts.md` |

---

## 1. Problem statement

Organizations need a lightweight support ticket system embedded in AEM so authors and support staff can create, track, assign, and comment on tickets without leaving the content platform. STMS stores tickets as JCR content, renders them through AEM components, and persists changes via secured OSGi services.

---

## 2. Stakeholders

| Stakeholder | Interest |
|---|---|
| **Support agent** | List, filter, assign, update, and comment on tickets |
| **Ticket creator** | Submit new tickets with title, description, priority, assignee |
| **AEM developer** | Maintainable Cloud Service–compliant codebase |
| **Platform operator** | Service-user security, repoinit, pipeline deploy |

---

## 3. Functional requirements

### FR-1 — Ticket storage

| ID | Requirement | Priority |
|---|---|---|
| FR-1.1 | Store tickets under `/content/stms/tickets` as JCR nodes | Must |
| FR-1.2 | Each ticket has: id, title, description, status, priority, assignee, creation date | Must |
| FR-1.3 | Comments stored as child nodes under each ticket | Must |
| FR-1.4 | Ticket IDs follow `TICKET-NNNN` sequential pattern | Must |
| FR-1.5 | New tickets default to status `open` | Must |

### FR-2 — Ticket listing

| ID | Requirement | Priority |
|---|---|---|
| FR-2.1 | Display all tickets in a list component | Must |
| FR-2.2 | Sort by creation date (ascending/descending) | Must |
| FR-2.3 | Filter by status, assignee, priority, creator | Must |
| FR-2.4 | Link from list row to ticket detail page | Must |
| FR-2.5 | Provide action to navigate to create-ticket page | Must |

### FR-3 — Ticket creation

| ID | Requirement | Priority |
|---|---|---|
| FR-3.1 | Form captures title, description, priority, assignee | Must |
| FR-3.2 | Client-side validation before submit | Should |
| FR-3.3 | Server-side validation (required fields, max lengths, valid priority) | Must |
| FR-3.4 | Redirect to detail page on success | Must |
| FR-3.5 | Redirect back to form with error message and preserved values on failure | Must |

### FR-4 — Ticket detail

| ID | Requirement | Priority |
|---|---|---|
| FR-4.1 | Display full ticket metadata and description | Must |
| FR-4.2 | Show comment thread ordered by date | Must |
| FR-4.3 | Resolve ticket by query parameter `ticketId` | Must |
| FR-4.4 | Link to edit-ticket page | Should |

### FR-5 — Ticket edit

| ID | Requirement | Priority |
|---|---|---|
| FR-5.1 | Edit title, description, status, priority, assignee | Must |
| FR-5.2 | Validate status and priority against allowed enums | Must |
| FR-5.3 | Redirect to detail page with success indicator on save | Must |

### FR-6 — Comments

| ID | Requirement | Priority |
|---|---|---|
| FR-6.1 | Add comment via form on detail page | Must |
| FR-6.2 | Author derived from logged-in AEM user | Must |
| FR-6.3 | Comment text max 5000 characters | Must |
| FR-6.4 | Redirect to detail with confirmation on success | Must |

### FR-7 — Application shell

| ID | Requirement | Priority |
|---|---|---|
| FR-7.1 | Sidebar navigation for workspace sections | Must |
| FR-7.2 | Top bar with branding and user context | Must |
| FR-7.3 | Configurable page paths for list and create via dialog | Must |

### FR-8 — Visual design

| ID | Requirement | Priority |
|---|---|---|
| FR-8.1 | Apply shared design tokens in base clientlib | Should |
| FR-8.2 | Component-scoped CSS for ticket UI | Should |
| FR-8.3 | Status badges and priority indicators | Should |

---

## 4. Non-functional requirements

| ID | Requirement | Target |
|---|---|---|
| NFR-1 | AEM Cloud Service compatibility | Pass AEM analyser; no deprecated APIs |
| NFR-2 | Write security | Service user `stms-ticket-service`; no admin sessions for persistence |
| NFR-3 | Module separation | Java in `core`; HTL in `ui.apps`; config in `ui.config` |
| NFR-4 | Search performance | Oak Lucene index on ticket properties |
| NFR-5 | Testability | Unit tests for repository, models, servlets |
| NFR-6 | Local development | Maven profiles for SDK deploy |
| NFR-7 | Production deploy | Cloud Manager Full Stack Pipeline |

---

## 5. Out of scope (baseline)

- Multi-tenant ticket isolation beyond AEM ACLs
- Email notifications on ticket events
- Granite Workflow approval chains (skill available; not implemented)
- Headless JSON API (form POST servlets only)
- Attachment uploads on tickets
- Delete / archive ticket workflow

---

## 6. Assumptions and constraints

| # | Assumption / constraint |
|---|---|
| A-1 | Authors are authenticated AEM users on author tier |
| A-2 | Ticket content path `/content/stms/tickets` is created via content package or repoinit |
| A-3 | `/libs` is immutable; all code under `/apps/stms` |
| A-4 | Java 21 per `.cloudmanager/java-version` |
| A-5 | Core WCM Components 2.28.0 for page scaffolding |

---

## 7. Requirements traceability

| Prompt (#) | Requirement IDs | Deliverable |
|---|---|---|
| 2–3 | FR-1 | JCR schema, `TicketModel`, `TicketRepository` |
| 4–5 | FR-2 | `ticketlist` component, `TicketListModel` |
| 6 | FR-4 | `ticketdetail` component |
| 7–8 | FR-3 | `ticketcreate` component, `TicketCreateServlet` |
| 9 | FR-6 | `ticketcomments` component, `TicketCommentServlet` |
| 10 | FR-5 | `ticketedit` component, `TicketEditServlet` |
| 11 | FR-8 | `clientlib-base` tokens and component CSS |
| 12 | FR-7 | `appshell` component, `AppShellModel` |

---

## 8. Open questions / future enhancements

1. Publish-tier read-only ticket views for external users?
2. Role-based field visibility (e.g. only managers change assignee)?
3. Integration with Adobe IMS groups for assignee picker?
4. Workflow-driven status transitions (Resolved → Closed approval)?
