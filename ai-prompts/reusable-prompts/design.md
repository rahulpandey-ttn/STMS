# AI Prompts — Design

Reusable prompts for **architecture and technical design** on the STMS AEM Cloud Service project.

**Tool:** Cursor (Ask or Plan mode recommended before coding)  
**Read first:** `design-notes.md`, `data-model.md`, `api-contract.md`, `ui-flow.md`

---

## When to use

- Designing JCR schema or new content types
- Choosing between servlet vs Sling Model vs workflow
- Component and dialog structure
- API shape for new write/read operations
- UI navigation and page flow

---

## Context to attach

| Artifact | Why |
|---|---|
| `design-notes.md` | Recorded architecture decisions (DD-1 … DD-8) |
| `data-model.md` | Ticket/comment node structure |
| `api-contract.md` | Existing servlet contracts |
| `ui-flow.md` | Page paths and user journeys |
| `.agents/skills/create-component/SKILL.md` | Component scaffolding rules |

---

## Prompt 1 — JCR schema design

```text
Design a JCR node schema for STMS under /content/stms/tickets (or a new path if justified).

Entity: [e.g. ticket attachment, ticket category]

Provide:
1. ASCII directory structure
2. sling:resourceType values (namespace stms/...)
3. Property list with types and required/optional
4. Parent/child relationships
5. Sling Model class outline (adaptables, injection)
6. Whether Oak index changes are needed in ui.config

Follow existing patterns: TicketModel, TicketCommentModel, nt:unstructured nodes.
AEM Cloud Service only — no /libs customization.
```

**STMS reference:** Foundation prompt #2 in `STMS-propmts-history.md`.

---

## Prompt 2 — Component design

```text
Design an AEM component for STMS:

Name: [component name]
Purpose: [what authors/users see and do]
Resource type: stms/components/[name]

Specify:
- HTL responsibilities vs Sling Model
- Dialog fields (Granite/Coral 3)
- Clientlib needs (JS validation, CSS)
- OSGi services to inject (@OSGiService)
- Sample page placement under ui.content

Reuse appshell wrapper where appropriate. Prefer Core WCM page component as parent page type.
Match existing ticket* components for form POST + redirect patterns.
```

---

## Prompt 3 — API / servlet design

```text
Design a write endpoint for STMS:

Operation: [create | update | delete | custom]
Resource: [tickets | comments | other]

Specify:
- Path under /bin/stms/...
- HTTP method (POST only for writes)
- Request parameters and validation rules
- Success redirect URL pattern
- Error redirect with preserved form fields
- TicketRepository method signature (do not persist in servlet)
- Service subservice: stms-ticket-write

Document in api-contract.md format. No JSON API unless explicitly requested.
```

---

## Prompt 4 — Repository layer design

```text
Extend TicketRepository for STMS:

New capability: [describe]

Provide:
- Interface method signatures on TicketRepository
- Request/Result DTO classes
- Validation rules (mirror TicketRepositoryImpl style)
- ID generation strategy if new child nodes
- QueryBuilder changes if list/search affected
- Unit test class names and scenarios

All writes via getTicketWriteResolver() and stms-ticket-write subservice.
```

---

## Prompt 5 — UI flow design

```text
Design the user flow for STMS feature "[feature name]".

Include:
1. Entry page (content path under /content/stms/us/en/...)
2. Step-by-step ASCII flow diagram
3. Query parameters and flash messages (created, updated, error)
4. App shell nav changes if any
5. Links between list | detail | create | edit pages

Align with ui-flow.md conventions and existing ticket page paths.
```

---

## Prompt 6 — Design decision record

```text
For STMS decision "[short title]", write a design decision record:

- Context
- Decision
- Rationale (bullets)
- Alternatives considered
- Trade-offs
- Cloud Service constraints

Format like DD-* sections in design-notes.md. Keep it concise.
```

---

## Prompt 7 — Visual / clientlib design

```text
Plan styling for STMS component "[component name]".

- Shared tokens: ui.apps/.../clientlib-base/css/tokens.css, badges.css, forms.css
- Component-scoped clientlib under components/[name]/clientlibs/
- Status/priority badge classes (match TicketModel.getStatusBadgeClass patterns)
- HTL structure for accessibility (labels, alerts)

Reference stms-tickets-redesign.html if provided. Do not duplicate site-wide CSS in component libs.
```

---

## Design constraints (always apply)

| Rule | Detail |
|---|---|
| Storage | `/content/stms/tickets` for tickets; extend schema, don't replace |
| Writes | `TicketRepository` + `stms-ticket-write` → `stms-ticket-service` |
| Modules | Java → `core`; HTL/dialogs → `ui.apps`; OSGi JSON → `ui.config` |
| APIs | Form POST + redirect unless headless explicitly required |
| UI | HTL + Granite Coral 3 dialogs; no new SPA framework |
| Search | QueryBuilder + `stms-ticket-index` for ticket queries |

---

## Design checklist (before implementation)

- [ ] JCR types and resource types defined
- [ ] Sling Model adaptables chosen (`Resource` vs `SlingHttpServletRequest`)
- [ ] Write path goes through repository, not servlet JCR access
- [ ] Redirect and error param strategy documented
- [ ] Page paths in ui.content identified
- [ ] Index/repoinit needs flagged for ui.config
- [ ] design-notes.md or data-model.md update noted if schema changes

---

## Related prompts

- **Planning:** `ai-prompts/planning.md`
- **Implementation:** `ai-prompts/implementation.md`
