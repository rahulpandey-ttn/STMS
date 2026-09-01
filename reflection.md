# Reflection

## What I Built

A working **Support Ticket Management System** on AEM as a Cloud Service:

- JCR-backed tickets at `/content/stms/tickets` with comment threads
- Six HTL components with Granite dialogs and clientlibs
- OSGi `TicketRepository` with QueryBuilder listing, filters, and sort
- Three Sling servlets for create, update, and comment
- Service-user security via repoinit and subservice mapping
- App shell with sidebar and top bar
- 18+ unit tests and `TicketCreateIT` integration scaffold
- Full assessment documentation set

---

## How I Used AI (across the lifecycle)

| Phase | Tool / approach |
|---|---|
| **Requirements** | Cursor Ask — gap analysis ("does list component exist?") |
| **Design** | Cursor Plan — JCR schema plan file before coding |
| **Implementation** | Cursor Agent — vertical slices with `@` reference files |
| **Testing** | Agent — "match `TicketRepositoryImplCreateTest` style" |
| **Debugging** | Agent + AEM MCP logs — service user and folder issues |
| **Review** | Ask mode — diff review against Cloud Service checklist |
| **Documentation** | Agent — structured docs; manual edit for personal voice |

Skills in `.agents/skills/` (`create-component`, etc.) were used as **reference playbooks**, not unattended codegen. I scoped every Agent run to specific modules and reference implementations.

---

## What AI Helped With Most

1. **Boilerplate speed** — Sling Model, servlet, and test scaffolding matching archetype patterns
2. **JCR schema design** — ASCII tree and model outline in Plan mode before implementation
3. **Error diagnosis** — mapping user-facing messages to `TicketRepositoryImpl` code paths
4. **Documentation structure** — initial drafts of api-contract, data-model, test-strategy
5. **QueryBuilder predicates** — filter/sort criteria in `TicketRepositoryImpl`

---

## What AI Got Wrong

| Issue | What happened | My correction |
|---|---|---|
| Deploy assumptions | AI sometimes suggested only `core` bundle deploy | I documented full package deploy (`ui.config`, `ui.content`) in debugging notes |
| Uniform documentation | Bulk-generated docs looked identical in tone | Restructured per template; added personal sections in reflection and prompt history |
| Over-scoping | Early suggestions to add JSON API + workflow in same sprint | Rejected; kept MVP to form POST + redirect |
| Test message drift | Generated test expected wrong validation string | Fixed assertion to match repository exactly |
| Missing authenticity | Retrospective doc pass without prompt log | Added `ai-prompts/history/` with accept/reject per entry |

---

## How I Validated AI Output

| Validation | Applied to |
|---|---|
| `mvn test -pl core` | All Java changes |
| `mvn clean install` | Full build + analyser |
| Local author smoke | UI flows after package deploy |
| AEM MCP logs | Runtime errors (service user) |
| CRX DE inspection | JCR node structure after create |
| Manual diff review | Every Agent-generated change |
| `TicketCreateIT` compile | Integration test scaffold |

I did **not** accept Agent output without running tests or checking against existing patterns (`TicketCreateServlet`, `TicketListModel`).

---

## What I Would Improve Next

1. **Capture prompts live** during implementation, not only at end
2. **Cypress E2E** for create → detail → comment
3. **`TicketEditIT` / `TicketCommentIT`** to match create IT
4. **Assignee picker** (Granite user field) instead of free text
5. **Granite Workflow** for status transitions (Resolved → Closed)
6. **Shorter docs** with links to code — less reverse-engineered prose
7. **Export raw Cursor chats** to `ai-prompts/exports/` for audit trail

---

## Reusable Workflow (prompts, rules, specs, templates)

| Artifact | Location | Reuse |
|---|---|---|
| Project AI rules | `AGENTS.md`, `.cursorrules` | Any AEM Cloud project |
| Prompt playbooks | `ai-prompts/planning.md` … `documentation.md` | Per-phase templates |
| Prompt history format | `ai-prompts/history/README.md` | Assessment / audit |
| JCR schema plan | `ai-prompts/ticket_jcr_schema_design_*.plan.md` | Similar CRUD features |
| Debugging template | `debugging-notes.md` | Per-incident append |
| API contract format | `api-contract.md` | Servlet-based AEM apps |
| IT pattern | `TicketCreateIT.java` | HTTP POST integration tests |
| Skills library | `.agents/skills/` | Dispatcher, workflow, components |

**Workflow loop:**

```text
Requirement prompt → Plan (design) → Agent (implement + test) →
MCP/debug if runtime fail → Ask (review) → Update prompt history
```

See also: `tool-workflow.md`, `implementation-plan.md` AI Usage Plan section.
