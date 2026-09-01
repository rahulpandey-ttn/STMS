# Requirement Analysis

## Selected Project Option

**STMS — Support Ticket Management System** on **Adobe Experience Manager as a Cloud Service**.

Authors and support staff need to create, list, filter, assign, update, and comment on support tickets inside AEM without an external ticketing database. Tickets are operational JCR content; the UI is built with HTL components and Granite dialogs.

---

## My Understanding (in your own words)

STMS is an author-side support desk embedded in AEM. Each ticket is a content node (not a page) with metadata and a comment thread. Users interact through a small app shell (sidebar + top bar) and ticket-specific components. Writes go through secured OSGi services using a dedicated service user — not the end user's JCR session directly. The goal is to demonstrate full-stack AEM Cloud Service delivery: JCR design, Sling Models, servlets, HTL, repoinit, indexing, and tests.

I treated this as a vertical-slice feature: every ticket capability spans `core` (Java), `ui.apps` (HTL/dialogs), and usually `ui.content` (sample pages). Cloud Service constraints (no `/libs` writes, pipeline deploy, Java 21) are non-negotiable.

---

## Functional Requirements

| ID | Requirement | Priority |
|---|---|---|
| FR-1 | Store tickets under `/content/stms/tickets` as JCR nodes with id, title, description, status, priority, assignee, created date | Must |
| FR-2 | Store comments as child nodes under each ticket | Must |
| FR-3 | List all tickets with sort by creation date | Must |
| FR-4 | Filter tickets by status, assignee, priority, creator | Must |
| FR-5 | Create ticket via form (title, description, priority, assignee) | Must |
| FR-6 | View ticket detail by `ticketId` query parameter | Must |
| FR-7 | Edit ticket (title, description, status, priority, assignee) | Must |
| FR-8 | Add comments on detail page; author from logged-in user | Must |
| FR-9 | App shell with sidebar navigation and top bar branding | Must |
| FR-10 | Client-side validation on create form; create action on list | Should |
| FR-11 | Shared visual design tokens in base clientlib | Should |

---

## Non-Functional Requirements

| ID | Requirement |
|---|---|
| NFR-1 | AEM Cloud Service compatible (AEM analyser, SDK API) |
| NFR-2 | Java 21; OSGi DS R6 |
| NFR-3 | Module separation: Java → `core`, HTL → `ui.apps`, OSGi JSON → `ui.config` |
| NFR-4 | Ticket writes via service user `stms-ticket-write` — no admin sessions |
| NFR-5 | QueryBuilder listing with Oak Lucene index for performance |
| NFR-6 | Unit tests for repository, models, servlets |
| NFR-7 | Deploy via Maven profiles to local SDK; production via Cloud Manager |
| NFR-8 | CSRF protection on POST forms |

---

## Assumptions

1. Users are authenticated AEM authors on the author tier.
2. `/content/stms/tickets` folder is created via content package or repoinit.
3. Ticket IDs use sequential `TICKET-NNNN` naming.
4. New tickets default to status `open`.
5. Assignee is a free-text field (email), not a Granite user picker (future enhancement).
6. No publish-tier write flows in baseline; author is the primary environment.
7. Form POST + HTTP redirect is acceptable (no headless JSON API required for MVP).

---

## Clarifications (questions for a product owner)

| # | Question | Default taken |
|---|---|---|
| Q-1 | Should ticket delete/archive be supported? | Out of scope for MVP |
| Q-2 | Who can edit assignee vs. status — all authors or role-based? | All authenticated authors |
| Q-3 | Email notifications on ticket events? | Out of scope |
| Q-4 | Granite Workflow for approval (e.g. Resolved → Closed)? | Future phase |
| Q-5 | Publish-tier read-only ticket views for external users? | Not in MVP |
| Q-6 | Attachment support on tickets? | Out of scope |

---

## Edge Cases

| Scenario | Expected behavior |
|---|---|
| Missing `ticketId` on detail page | Empty / not-found state; no server error |
| Unknown `ticketId` | Repository returns empty; UI shows not found |
| Title > 200 characters | Server validation error; form values preserved |
| Comment > 5000 characters | Server validation error |
| Invalid status or priority enum | Server validation error on update/create |
| `/content/stms/tickets` missing | "Tickets folder is not configured." |
| Service user not mapped | "Ticket service is not available." |
| GET on write servlet | HTTP 405 Method Not Allowed |
| Ticket node without `sling:resourceType` | Excluded from QueryBuilder list |
| Empty filter on list | Show all tickets (subject to sort) |

---

## Traceability

Original feature prompts: `ai-prompts/STMS-propmts-history.md`  
Design plan output: `ai-prompts/ticket_jcr_schema_design_24dcc4d5.plan.md`  
Acceptance criteria: `acceptance-criteria.md`
