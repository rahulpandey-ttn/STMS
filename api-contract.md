# API Contract

Base URL (local author): `http://localhost:4502`

All write endpoints: **POST only**, `application/x-www-form-urlencoded`, Granite CSRF token required, persistence via `TicketRepository` + service user `stms-ticket-write`.

---

## Endpoint: Create Ticket

**Method:** `POST`  
**Path:** `/bin/stms/ticket/create`  
**Purpose:** Create a new ticket under `/content/stms/tickets` with default status `open` and empty `comments` container.

### Request

| Parameter | Required | Type | Description |
|---|---|---|---|
| `title` | Yes | string | Max 200 characters |
| `description` | Yes | string | Ticket body |
| `priority` | Yes | enum | `low`, `medium`, `high`, `critical` |
| `assignee` | No | string | Email or identifier |
| `detailPage` | Yes* | path | Redirect on success (no `.html`) |
| `formPage` | Yes* | path | Redirect on error |
| `:cq_csrf_token` | Yes | string | Granite CSRF |

### Response

**Success (302):**

```http
Location: {detailPage}.html?ticketId=TICKET-NNNN&created=true
```

**Error (302):**

```http
Location: {formPage}.html?error={message}&title=...&description=...
```

### Validation Rules

- Title required; max 200 chars
- Description required
- Priority must match `TicketPriority` enum

### Error Responses

| Message | Condition |
|---|---|
| Title is required. | Blank title |
| Title must be 200 characters or fewer. | Length |
| Description is required. | Blank description |
| A valid priority is required. | Invalid enum |
| Tickets folder is not configured. | Missing `/content/stms/tickets` |
| Ticket service is not available. | Service user login failure |
| Unable to save the ticket. Please try again. | Persistence exception |

---

## Endpoint: Update Ticket

**Method:** `POST`  
**Path:** `/bin/stms/ticket/update`  
**Purpose:** Update an existing ticket's metadata.

### Request

| Parameter | Required | Type | Description |
|---|---|---|---|
| `ticketId` | Yes | string | e.g. `TICKET-0001` |
| `title` | Yes | string | Max 200 |
| `description` | Yes | string | |
| `status` | Yes | enum | `open`, `in-progress`, `resolved`, `closed` |
| `priority` | Yes | enum | `low`, `medium`, `high`, `critical` |
| `assignee` | No | string | |
| `detailPage` | Yes* | path | |
| `formPage` | Yes* | path | |
| `:cq_csrf_token` | Yes | string | |

### Response

**Success (302):**

```http
Location: {detailPage}.html?ticketId={id}&updated=true
```

### Validation Rules

- Same as create for title/description/priority
- Status must match `TicketStatus` enum
- Ticket must exist

### Error Responses

| Message | Condition |
|---|---|
| Ticket ID is required. | Blank id |
| Ticket was not found. | Missing node |
| A valid status is required. | Invalid enum |

---

## Endpoint: Add Comment

**Method:** `POST`  
**Path:** `/bin/stms/ticket/comment`  
**Purpose:** Append a comment to a ticket's `comments` container.

### Request

| Parameter | Required | Type | Description |
|---|---|---|---|
| `ticketId` | Yes | string | Parent ticket |
| `text` | Yes | string | Max 5000 chars |
| `detailPage` | Yes* | path | |
| `formPage` | Yes* | path | |
| `:cq_csrf_token` | Yes | string | |

> `author` is resolved server-side from the logged-in AEM user.

### Response

**Success (302):**

```http
Location: {detailPage}.html?ticketId={id}&commentAdded=true
```

### Validation Rules

- Ticket ID, author, and text required
- Text max 5000 characters

### Error Responses

| Message | Condition |
|---|---|
| Comment text is required. | Blank |
| Comment must be 5000 characters or fewer. | Length |
| Ticket was not found. | Missing parent |

---

## Read API (page-level, not servlet)

Ticket reads use Sling Models on page render — not REST endpoints.

**List filters (GET query params on list page):**

| Param | Values |
|---|---|
| `status` | `open`, `in-progress`, `resolved`, `closed` |
| `assignee` | email string |
| `priority` | `low`, `medium`, `high`, `critical` |
| `creator` | AEM username |
| `sort` | `createdDate-asc`, `createdDate-desc` |

**Detail:** `?ticketId=TICKET-NNNN`

Implementation: `TicketListModel`, `TicketDetailModel`, `TicketRepository.findTickets()` / `getTicket()`.

---

## Implementation map

| Path | Servlet | Repository method |
|---|---|---|
| `/bin/stms/ticket/create` | `TicketCreateServlet` | `createTicket()` |
| `/bin/stms/ticket/update` | `TicketEditServlet` | `updateTicket()` |
| `/bin/stms/ticket/comment` | `TicketCommentServlet` | `addComment()` |

See also: `data-model.md`
