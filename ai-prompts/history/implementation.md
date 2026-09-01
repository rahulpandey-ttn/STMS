# Prompt History — Implementation

---

## P-IMP-001 | Execute schema plan

**Date:** 2026-08-31  
**Mode:** Agent

### Prompt

```text
Implement the Support Ticket JCR Schema and Sling Model Architecture plan as specified.
Do not edit the plan file. Complete all existing to-dos in order.
```

### AI response

Implemented enums, models, `TicketRepositoryImpl`, sample content paths, unit tests, Oak index.

### Accepted

- Full Phase 1 file set in `core` and `ui.config`
- Test classes `TicketModelTest`, `TicketRepositoryImplTest`

### Changed

- Adjusted package to `com.ttn.stms.core.tickets.*` consistently
- Ran `mvn test -pl core` and fixed one QueryBuilder predicate

### Rejected

- AI attempt to edit plan file todos — instruction said do not edit plan

---

## P-IMP-002 | Ticket list component

**Date:** 2026-08-31  
**Mode:** Agent  
**Context:** `@TicketListModel.java` (reference), `create-component` skill

### Prompt

```text
Create a ticket listing component that displays all tickets, supports sorting by
creation time, and allows filtering by creator, status, and related fields.
Touch core and ui.apps only.
```

### AI response

`ticketlist` HTL, dialog, `TicketListModel` with `TicketSearchCriteria`, clientlib CSS.

### Accepted

- Component resource type `stms/components/ticketlist`
- URL param filters matching `PARAM_STATUS`, `PARAM_ASSIGNEE`, etc.

### Changed

- Wired `detailPage` and `createPage` dialog properties from ui.content sample

### Rejected

- Client-side-only filtering — server-side QueryBuilder required for scale

---

## P-IMP-003 | Create ticket vertical slice

**Date:** 2026-09-01  
**Mode:** Agent  
**Context:** `@TicketCreateServlet.java` pattern

### Prompt

```text
Build a component to create a new ticket. Follow TicketCreateServlet redirect pattern.
Include server validation in TicketRepositoryImpl.
```

### AI response

`ticketcreate` component, servlet, DTOs, `TicketCreateServletTest`.

### Accepted

- POST `/bin/stms/ticket/create`
- Redirect to detail with `ticketId` and `created=true`
- Error redirect with preserved form fields

### Changed

- Added `:cq_csrf_token` to HTL after review

### Rejected

- AJAX JSON response instead of redirect — inconsistent with other forms

---

## P-IMP-004 | Edit and comments

**Date:** 2026-09-01  
**Mode:** Agent

### Prompt

```text
Create a component for adding comments to a ticket.
Then add the ability to edit an existing ticket.
Match patterns from ticketcreate and TicketRepositoryImpl.createTicket.
```

### AI response

`ticketcomments`, `ticketedit`, servlets, `addComment`, `updateTicket` in repository.

### Accepted

- Comment author resolved from logged-in user in servlet
- Edit supports all status enum values

### Changed

- Comment max length 5000 enforced in repository (not servlet)

### Rejected

- AI suggested storing author display name only — kept email/user id from session

---

## P-IMP-005 | App shell

**Date:** 2026-09-01  
**Mode:** Agent

### Prompt

```text
Add a sidebar and top bar to the application shell.
Wrap existing ticket pages with stms/components/appshell.
```

### AI response

`AppShellModel`, `appshell` HTL/CSS/JS, ui.content page updates.

### Accepted

- Dialog props `ticketsListPage`, `createTicketPage`
- Nav items for workspace sections

### Changed

- Simplified nav to tickets-focused links for MVP demo

### Rejected

- Dynamic nav from JCR tree — overkill for assessment scope
