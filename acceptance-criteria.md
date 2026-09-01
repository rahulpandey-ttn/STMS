# Acceptance Criteria — STMS

Testable criteria for the Support Ticket Management System. Each item maps to implemented behavior in the STMS codebase.

---

## AC-1 — Ticket persistence

| # | Given | When | Then |
|---|---|---|---|
| AC-1.1 | Valid create form submission | User submits title, description, priority | A node `TICKET-NNNN` exists under `/content/stms/tickets` with `sling:resourceType=stms/tickets/ticket` |
| AC-1.2 | Ticket created | Inspecting node properties | `status=open`, `createdDate` set, `ticketId` matches node name |
| AC-1.3 | Ticket created | Inspecting children | `comments` child exists with `sling:resourceType=stms/tickets/comments` |
| AC-1.4 | Missing title | User submits create form | Error: "Title is required."; no node created |
| AC-1.5 | Title > 200 chars | User submits create form | Error: "Title must be 200 characters or fewer." |
| AC-1.6 | Invalid priority | User submits create form | Error: "A valid priority is required." |

---

## AC-2 — Ticket list

| # | Given | When | Then |
|---|---|---|---|
| AC-2.1 | Tickets exist in repository | User opens `/content/stms/us/en/tickets.html` | All tickets render in list |
| AC-2.2 | Multiple tickets | User selects sort `createdDate-desc` | Newest ticket appears first |
| AC-2.3 | Tickets with varied status | User filters `status=open` | Only open tickets shown |
| AC-2.4 | Tickets with assignees | User filters `assignee=user@example.com` | Only matching assignee shown |
| AC-2.5 | List page configured | User clicks "Create ticket" | Navigates to create-ticket page |
| AC-2.6 | List row rendered | User clicks ticket link | Navigates to detail page with `ticketId` param |

---

## AC-3 — Ticket detail

| # | Given | When | Then |
|---|---|---|---|
| AC-3.1 | Valid `ticketId` query param | User opens detail page | Title, description, status, priority, assignee, dates displayed |
| AC-3.2 | Ticket has comments | User opens detail page | Comments listed chronologically |
| AC-3.3 | Invalid/missing `ticketId` | User opens detail page | Empty or not-found state (no crash) |
| AC-3.4 | Ticket exists | User clicks edit link | Navigates to edit page with ticket context |

---

## AC-4 — Ticket create

| # | Given | When | Then |
|---|---|---|---|
| AC-4.1 | Valid form + configured detail page | POST to `/bin/stms/ticket/create` | 302 redirect to detail `.html?ticketId=TICKET-NNNN&created=true` |
| AC-4.2 | Validation failure | POST with missing description | Redirect to form with `error` param and field values preserved |
| AC-4.3 | Create form loaded | Client-side validation runs | Required fields enforced before submit |
| AC-4.4 | CSRF protection | Form submitted | `:cq_csrf_token` included |

---

## AC-5 — Ticket edit

| # | Given | When | Then |
|---|---|---|---|
| AC-5.1 | Existing ticket | User updates status to `in-progress` and saves | JCR `status` property updated |
| AC-5.2 | Valid update | POST to `/bin/stms/ticket/update` | Redirect to detail with `updated=true` |
| AC-5.3 | Invalid status value | POST update | Error returned; ticket unchanged |
| AC-5.4 | Unknown ticket ID | POST update | Error: "Ticket was not found." |

---

## AC-6 — Comments

| # | Given | When | Then |
|---|---|---|---|
| AC-6.1 | Logged-in author on detail page | User submits comment text | Comment node under `comments/` with author, text, `createdDate` |
| AC-6.2 | Comment added | Redirect completes | Detail page shows `commentAdded=true` |
| AC-6.3 | Empty comment text | User submits | Error: "Comment text is required." |
| AC-6.4 | Comment > 5000 chars | User submits | Error: "Comment must be 5000 characters or fewer." |

---

## AC-7 — Application shell

| # | Given | When | Then |
|---|---|---|---|
| AC-7.1 | Page uses `appshell` component | User loads any STMS app page | Sidebar and top bar visible |
| AC-7.2 | User on tickets list | Shell renders | Active nav highlights tickets section |
| AC-7.3 | Dialog configured paths | User clicks nav item | Correct list/create page opens |

---

## AC-8 — Security and infrastructure

| # | Given | When | Then |
|---|---|---|---|
| AC-8.1 | Package deployed to author | `stms.core` bundle status | Bundle `Active` |
| AC-8.2 | Repoinit applied | Service user check | `stms-ticket-service` exists with write ACL on `/content/stms/tickets` |
| AC-8.3 | Write operation | Servlet persists ticket | Uses `stms-ticket-write` subservice (not request resolver) |
| AC-8.4 | GET on write endpoints | Client sends GET | HTTP 405 Method Not Allowed |

---

## AC-9 — Build and quality

| # | Given | When | Then |
|---|---|---|---|
| AC-9.1 | Clean workspace | `mvn test -pl core` | All unit tests pass |
| AC-9.2 | Code changes | `mvn clean install` | Build succeeds; AEM analyser passes |
| AC-9.3 | Dispatcher changes | `./bin/validate.sh src` | No validation errors |

---

## Definition of done (per feature)

- [ ] Acceptance criteria for the feature pass on local AEM author
- [ ] Unit tests added or updated in `core/src/test/java`
- [ ] No secrets or hardcoded production credentials
- [ ] Changes limited to appropriate modules (`core`, `ui.apps`, `ui.config`, `ui.content`)
- [ ] Documented in relevant root docs if API or data model changes
