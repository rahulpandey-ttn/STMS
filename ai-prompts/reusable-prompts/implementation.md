# AI Prompts — Implementation

Reusable prompts for **code generation and delivery** on the STMS AEM Cloud Service project.

**Tool:** Cursor Agent mode  
**Read first:** `AGENTS.md`, `.cursorrules`, `implementation-plan.md`, `design-notes.md`

---

## When to use

- Building or extending ticket features end-to-end
- Adding AEM components, servlets, or OSGi config
- Following an approved plan or design
- Minimal-diff fixes in `core` or `ui.apps`

---

## Context to attach

| Artifact | Why |
|---|---|
| Reference implementation | e.g. `@TicketCreateServlet.java`, `@ticketcreate/` |
| `data-model.md` | Property names and resource types |
| `api-contract.md` | Servlet params and redirects |
| `.agents/skills/create-component/SKILL.md` | New components |
| `implementation-plan.md` | Phase and file list |

---

## Prompt 1 — Vertical slice (ticket feature)

```text
Implement [feature] for STMS following existing ticket vertical slice patterns.

Reference:
- TicketRepositoryImpl for persistence
- TicketCreateServlet / ticketcreate for form POST + redirect
- TicketListModel / ticketlist for read + filters

Touch only: [list modules, e.g. core, ui.apps]
Do not edit plan files.

After changes:
1. mvn test -pl core
2. Summarize files changed and how to verify on http://localhost:4502/content/stms/us/en/tickets.html
```

---

## Prompt 2 — New ticket field (end-to-end)

```text
Add ticket field "[fieldName]" to STMS:

- Type: [string | enum | date | ...]
- Required: [yes/no]
- Shown on: [list | detail | create | edit]

Update:
1. TicketModel + TicketRepositoryImpl (create + update)
2. DTOs and validation (max length if string)
3. Servlets if form param added
4. HTL + _cq_dialog for affected components: [ticketcreate, ticketedit, ticketdetail, ticketlist]
5. Unit tests in core/src/test/java/.../tickets/

Match coding style of existing files. Run mvn test -pl core.
```

---

## Prompt 3 — New AEM component

```text
Create STMS component "[name]" using create-component skill conventions.

Resource type: stms/components/[name]
Package: com.ttn.stms.core.[package]

Include:
- .content.xml, HTL, _cq_dialog (Coral 3)
- Sling Model in core if logic needed
- Component clientlibs if JS/CSS required
- Unit test with AemContext

Component group: STMS. Do not modify unrelated components.
```

---

## Prompt 4 — Repository + servlet only

```text
Add OSGi capability to STMS without UI:

1. Extend TicketRepository interface and TicketRepositoryImpl
2. Add [TicketXServlet] at /bin/stms/ticket/[action]
3. POST only, delegate to repository, redirect on success/error
4. Unit tests: repository test + servlet test

Use stms-ticket-write subservice for all JCR writes.
No loginAdministrative. DS R6 annotations only.
```

---

## Prompt 5 — ui.config change

```text
Update STMS ui.config for:

[repoinit ACL | service user mapping | Oak index | logging | CORS]

Files under: ui.config/src/main/content/jcr_root/apps/stms/osgiconfig/

Explain runtime effect on TicketRepositoryImpl writes.
Do not change Java unless subservice name changes (then update TICKET_WRITE_SUBSERVICE constant).
```

---

## Prompt 6 — Sample content page

```text
Add sample STMS page under ui.content:

Path: /content/stms/us/en/[path]
Template: /conf/stms/settings/wcm/templates/page-content
Structure: appshell → [child components]

Copy dialog props from existing tickets pages (ticketsListPage, createTicketPage, detailPage).
Match .content.xml style in ui.content/.../tickets/.content.xml
```

---

## Prompt 7 — Implement from plan (no plan edits)

```text
Implement the STMS plan for "[feature name]" as specified.

Rules:
- Do not edit the plan file or ai-prompts/*.plan.md files
- Complete tasks in plan order
- Mark todos in_progress / completed as you work
- Stop and report if a task is blocked

Deploy verification: mvn clean install -pl core,ui.apps -PautoInstallPackage
```

**STMS reference:** Prompt #3 in `STMS-propmts-history.md`.

---

## Prompt 8 — Frontend / clientlib

```text
Update STMS styling for [component | site]:

- Component CSS/JS: ui.apps/.../components/[name]/clientlibs/
- Shared tokens: ui.apps/.../clientlib-base/css/
- Or ui.frontend build → clientlib-site if site-wide

Run: cd ui.frontend && npm run dev (if ui.frontend touched)
Match existing badge/form patterns from ticket components.
```

---

## Implementation rules (agent must follow)

| Rule | Detail |
|---|---|
| Minimal scope | Only files required for the feature |
| Conventions | Match neighboring classes (naming, imports, test style) |
| Writes | `TicketRepositoryImpl` + `stms-ticket-write` |
| Servlets | Thin: parse → repository → redirect |
| HTL | No business logic; use Sling Models |
| Tests | Update `core` unit tests when behavior changes |
| Build | Run `mvn test -pl core` before finishing |

---

## Deploy commands (reference)

```bash
# Bundle only
mvn clean install -pl core -PautoInstallBundle

# UI + core package
mvn clean install -pl ui.apps,core -PautoInstallPackage

# Full project
mvn clean install -PautoInstallSinglePackage
```

---

## Implementation checklist

- [ ] Code in correct module (`core` / `ui.apps` / `ui.config` / `ui.content`)
- [ ] Resource types and paths match `data-model.md`
- [ ] Forms include `:cq_csrf_token` where POSTing
- [ ] Unit tests added or updated
- [ ] `mvn test -pl core` passes
- [ ] Manual smoke path documented for author

---

## Related prompts

- **Testing:** `ai-prompts/testing.md`
- **Debugging:** `ai-prompts/debugging.md`
- **Code review:** `ai-prompts/code-review.md`
