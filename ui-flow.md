# UI Flow — STMS

End-user journeys and page navigation for the Support Ticket Management System.

---

## 1. Page map

| Page | Content path | Primary component(s) |
|---|---|---|
| **Ticket list** | `/content/stms/us/en/tickets` | `appshell` → `ticketlist` |
| **Create ticket** | `/content/stms/us/en/tickets/create-ticket` | `appshell` → `ticketcreate` |
| **Edit ticket** | `/content/stms/us/en/tickets/edit-ticket` | `appshell` → `ticketedit` |
| **Ticket detail** | `/content/stms/us/en/ticket-detail` | `appshell` → `ticketdetail`, `ticketcomments` |

Local URLs append `.html` (e.g. `http://localhost:4502/content/stms/us/en/tickets.html`).

---

## 2. Application shell

All ticket pages share `stms/components/appshell`:

```text
┌────────────────────────────────────────────────────────────┐
│  Top bar — brand title, user display name, role            │
├──────────────┬─────────────────────────────────────────────┤
│   Sidebar    │              Main content area              │
│              │                                             │
│  • Tickets   │   (ticketlist | ticketcreate | ticketedit │
│  • Create    │    | ticketdetail + ticketcomments)       │
│              │                                             │
└──────────────┴─────────────────────────────────────────────┘
```

**Dialog configuration (per page):**

| Property | Purpose |
|---|---|
| `ticketsListPage` | Path to list page |
| `createTicketPage` | Path to create page |
| `brandTitle` / `brandSubtitle` | Header branding |
| `userDisplayName` / `userRole` | Top bar user context |

---

## 3. Flow: View ticket list

```text
[User opens Tickets page]
        │
        ▼
┌───────────────────┐
│  App shell loads  │
│  + ticketlist     │
└─────────┬─────────┘
          │
          ▼
┌───────────────────────────────────────┐
│ TicketListModel queries repository    │
│ (optional filters from URL params)    │
└─────────┬─────────────────────────────┘
          │
          ▼
┌───────────────────────────────────────┐
│ HTL renders table/cards with:         │
│  • title, status badge, priority      │
│  • assignee avatar, created date      │
│  • link → detail page                 │
└─────────┬─────────────────────────────┘
          │
    ┌─────┴─────┐
    │           │
    ▼           ▼
[Filter/sort] [Create ticket btn]
 URL params     → create page
```

**Filter controls:** Update URL with `status`, `assignee`, `priority`, `creator`, `sort` — page reloads with filtered list.

---

## 4. Flow: Create ticket

```text
[User clicks "Create ticket"]
        │
        ▼
┌───────────────────┐
│  ticketcreate form │
│  title, desc,      │
│  priority, assignee│
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐     fail    ┌────────────────────────┐
│ Client-side       │────────────►│ Show inline errors     │
│ validation (JS)   │             │ (block submit)         │
└─────────┬─────────┘             └────────────────────────┘
          │ pass
          ▼
┌───────────────────┐
│ POST /bin/stms/   │
│ ticket/create     │
└─────────┬─────────┘
          │
    ┌─────┴─────┐
    │           │
 success      error
    │           │
    ▼           ▼
[Redirect to   [Redirect to create page
 detail.html    with ?error=...& preserved fields]
 ?ticketId=...&
 created=true]
```

**Success state:** Detail page shows confirmation banner (`created=true`).

---

## 5. Flow: View ticket detail

```text
[User opens detail link with ?ticketId=TICKET-0001]
        │
        ▼
┌───────────────────┐
│ TicketDetailModel │
│ loads ticket +    │
│ comments          │
└─────────┬─────────┘
          │
          ▼
┌───────────────────────────────────────┐
│ Detail view shows:                    │
│  • metadata (status, priority, etc.)  │
│  • description                        │
│  • comment thread                     │
│  • Edit button → edit page            │
└─────────┬─────────────────────────────┘
          │
          ▼
┌───────────────────┐
│ ticketcomments    │
│ form at bottom    │
└───────────────────┘
```

**Missing ticket:** Model returns empty; HTL shows not-found messaging.

---

## 6. Flow: Edit ticket

```text
[User clicks Edit on detail page]
        │
        ▼
┌───────────────────┐
│ ticketedit form   │
│ pre-filled from   │
│ TicketEditModel   │
└─────────┬─────────┘
          │
          ▼
┌───────────────────┐
│ POST /bin/stms/   │
│ ticket/update     │
└─────────┬─────────┘
          │
    ┌─────┴─────┐
 success      error
    │           │
    ▼           ▼
[detail.html    [edit page with error
 ?updated=true]  and preserved values]
```

**Editable fields:** title, description, status, priority, assignee.

---

## 7. Flow: Add comment

```text
[User on detail page, enters comment text]
        │
        ▼
┌───────────────────┐
│ POST /bin/stms/   │
│ ticket/comment    │
│ (author = server) │
└─────────┬─────────┘
          │
    ┌─────┴─────┐
 success      error
    │           │
    ▼           ▼
[detail.html    [detail with ?error=...]
 ?commentAdded=
 true]
```

Comment appears in thread after redirect (sorted by `createdDate` in `TicketModel`).

---

## 8. Navigation matrix

| From | Action | To |
|---|---|---|
| List | Click ticket row | Detail (`?ticketId=`) |
| List | Click "Create ticket" | Create page |
| List | Apply filter/sort | Same page (new query string) |
| Detail | Click "Edit" | Edit page (`?ticketId=`) |
| Detail | Submit comment | Detail (refresh with flash) |
| Create | Submit form (success) | Detail (`?ticketId=&created=true`) |
| Create | Submit form (error) | Create (with error params) |
| Edit | Submit form (success) | Detail (`?updated=true`) |
| Edit | Submit form (error) | Edit (with error params) |
| Shell sidebar | Tickets link | List page |
| Shell sidebar | Create link | Create page |

---

## 9. Visual states

| State | UI indicator |
|---|---|
| Status: Open | Badge class `open` |
| Status: In Progress | Badge class `progress` |
| Status: Resolved | Badge class `resolved` |
| Status: Closed | Badge class `closed` |
| Priority: High/Critical | Indicator class `high` |
| Priority: Medium | Indicator class `medium` |
| Priority: Low | Indicator class `low` |
| Assignee present | Avatar with initials + color variant |
| Form error | Alert with `error` message from query param |
| Success flash | Banner for `created`, `updated`, `commentAdded` |

Shared styles: `ui.apps/.../clientlib-base/css/` (`tokens.css`, `badges.css`, `forms.css`, `alerts.css`).

---

## 10. Author vs publish

| Tier | Current behavior |
|---|---|
| **Author** | Full CRUD via components and servlets |
| **Publish** | Sample content may exist; write servlets typically author-only |

Future publish read-only views would require dispatcher rules and read-only components.

---

## 11. Accessibility and UX notes

- Forms use semantic HTML with labels tied to inputs
- CSRF token included for POST security
- Error messages preserved on redirect so users don't re-enter all fields
- Client-side validation provides immediate feedback before server round-trip

---

## 12. Related documents

- `api-contract.md` — POST parameters and redirect URLs
- `data-model.md` — JCR properties displayed in UI
- `acceptance-criteria.md` — Testable UI outcomes
