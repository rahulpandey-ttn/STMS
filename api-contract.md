# API Contract — STMS

Servlet API for ticket write operations. All endpoints are **author-tier**, **POST-only**, and use **HTML form encoding** with **HTTP redirect** responses.

Base URL (local): `http://localhost:4502`

---

## Common behavior

| Aspect | Rule |
|---|---|
| **Method** | `POST` only; `GET` returns `405 Method Not Allowed` |
| **Content-Type** | `application/x-www-form-urlencoded` |
| **Authentication** | AEM author session (cookie) |
| **CSRF** | Include `:cq_csrf_token` (Granite CSRF) on all forms |
| **Success response** | `302 Found` redirect to configured page |
| **Error response** | `302 Found` redirect to form page with `error` query param |
| **Persistence** | Delegated to `TicketRepository` via service user `stms-ticket-write` |

---

## 1. Create ticket

### `POST /bin/stms/ticket/create`

Creates a new support ticket under `/content/stms/tickets`.

#### Request parameters

| Parameter | Required | Type | Description |
|---|---|---|---|
| `title` | Yes | string | Ticket title (max 200 characters) |
| `description` | Yes | string | Ticket body text |
| `priority` | Yes | enum | `low`, `medium`, `high`, `critical` |
| `assignee` | No | string | Assignee identifier (typically email) |
| `detailPage` | Yes* | path | Content path for redirect on success (no `.html`) |
| `formPage` | Yes* | path | Content path for redirect on error |
| `:cq_csrf_token` | Yes | string | Granite CSRF token |

\*Required for correct redirect behavior; servlet returns error redirect if `detailPage` missing on success.

#### Success response

```
HTTP/1.1 302 Found
Location: {detailPage}.html?ticketId={TICKET-NNNN}&created=true
```

#### Error response

```
HTTP/1.1 302 Found
Location: {formPage}.html?error={encodedMessage}&title=...&description=...&priority=...&assignee=...
```

#### Validation errors

| Message | Condition |
|---|---|
| Title is required. | Blank title |
| Title must be 200 characters or fewer. | Title length > 200 |
| Description is required. | Blank description |
| A valid priority is required. | Unknown priority value |
| Tickets folder is not configured. | `/content/stms/tickets` missing |
| Ticket service is not available. | Service user login failure |
| Unable to save the ticket. Please try again. | JCR persistence exception |

#### Side effects

- Creates node `/content/stms/tickets/TICKET-NNNN`
- Sets `status=open`, `createdDate=now`
- Creates empty `comments` child container

---

## 2. Update ticket

### `POST /bin/stms/ticket/update`

Updates an existing ticket.

#### Request parameters

| Parameter | Required | Type | Description |
|---|---|---|---|
| `ticketId` | Yes | string | Node name (e.g. `TICKET-0001`) |
| `title` | Yes | string | Updated title (max 200 characters) |
| `description` | Yes | string | Updated description |
| `status` | Yes | enum | `open`, `in-progress`, `resolved`, `closed` |
| `priority` | Yes | enum | `low`, `medium`, `high`, `critical` |
| `assignee` | No | string | Updated assignee |
| `detailPage` | Yes* | path | Redirect target on success |
| `formPage` | Yes* | path | Redirect target on error |
| `:cq_csrf_token` | Yes | string | Granite CSRF token |

#### Success response

```
HTTP/1.1 302 Found
Location: {detailPage}.html?ticketId={ticketId}&updated=true
```

#### Error response

```
HTTP/1.1 302 Found
Location: {formPage}.html?error={encodedMessage}&ticketId=...&...
```

#### Validation errors

| Message | Condition |
|---|---|
| Ticket ID is required. | Blank ticketId |
| Title is required. | Blank title |
| Title must be 200 characters or fewer. | Title length > 200 |
| Description is required. | Blank description |
| A valid status is required. | Unknown status |
| A valid priority is required. | Unknown priority |
| Ticket was not found. | No node at `/content/stms/tickets/{ticketId}` |

---

## 3. Add comment

### `POST /bin/stms/ticket/comment`

Adds a comment to an existing ticket.

#### Request parameters

| Parameter | Required | Type | Description |
|---|---|---|---|
| `ticketId` | Yes | string | Parent ticket node name |
| `text` | Yes | string | Comment body (max 5000 characters) |
| `detailPage` | Yes* | path | Redirect target on success |
| `formPage` | Yes* | path | Redirect target on error |
| `:cq_csrf_token` | Yes | string | Granite CSRF token |

> **Note:** `author` is resolved server-side from the logged-in AEM user (`TicketCommentServlet.resolveAuthor`). It is not submitted by the client.

#### Success response

```
HTTP/1.1 302 Found
Location: {detailPage}.html?ticketId={ticketId}&commentAdded=true
```

#### Error response

```
HTTP/1.1 302 Found
Location: {formPage}.html?error={encodedMessage}&ticketId=...&text=...
```

#### Validation errors

| Message | Condition |
|---|---|
| Ticket ID is required. | Blank ticketId |
| Author is required. | Cannot resolve current user |
| Comment text is required. | Blank text |
| Comment must be 5000 characters or fewer. | Text length > 5000 |
| Ticket was not found. | Parent ticket missing |

#### Side effects

- Creates node `/content/stms/tickets/{ticketId}/comments/comment-{timestamp}-{seq}`
- Sets `author`, `text`, `createdDate`

---

## 4. Read API (not servlet-based)

Ticket **read** operations are not exposed as REST endpoints. Data is loaded via:

| Mechanism | Usage |
|---|---|
| **Sling Models** | `TicketListModel`, `TicketDetailModel` on page render |
| **TicketRepository** | `getTicket()`, `findTickets()` called from models |
| **Query parameters** | List filters: `status`, `assignee`, `priority`, `creator`, `sort` |
| **Detail parameter** | `ticketId` on detail/edit pages |

### List query parameters (GET on page URL)

| Parameter | Values | Default |
|---|---|---|
| `status` | `open`, `in-progress`, `resolved`, `closed` | (none — all) |
| `assignee` | email string | (none — all) |
| `priority` | `low`, `medium`, `high`, `critical` | (none — all) |
| `creator` | AEM username | (none — all) |
| `sort` | `createdDate-asc`, `createdDate-desc` | `createdDate-desc` |

Example:

```
/content/stms/us/en/tickets.html?status=open&sort=createdDate-desc
```

### Detail query parameters

| Parameter | Description |
|---|---|
| `ticketId` | Ticket node name (e.g. `TICKET-0001`) |
| `created` | `true` — flash after create |
| `updated` | `true` — flash after edit |
| `commentAdded` | `true` — flash after comment |

---

## 5. Enum reference

### Status (`TicketStatus`)

| Value | Label |
|---|---|
| `open` | Open |
| `in-progress` | In Progress |
| `resolved` | Resolved |
| `closed` | Closed |

### Priority (`TicketPriority`)

| Value | Label |
|---|---|
| `low` | Low |
| `medium` | Medium |
| `high` | High |
| `critical` | Critical |

---

## 6. Implementation map

| Endpoint | Servlet class | Repository method |
|---|---|---|
| `/bin/stms/ticket/create` | `TicketCreateServlet` | `createTicket()` |
| `/bin/stms/ticket/update` | `TicketEditServlet` | `updateTicket()` |
| `/bin/stms/ticket/comment` | `TicketCommentServlet` | `addComment()` |

---

## 7. Future API extensions (not implemented)

| Endpoint | Method | Purpose |
|---|---|---|
| `/bin/stms/ticket.json` | GET | Headless ticket read |
| `/bin/stms/ticket/delete` | POST | Archive/delete ticket |
| `/bin/stms/ticket/list.json` | GET | Paginated JSON list |
