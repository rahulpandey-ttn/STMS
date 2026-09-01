# Implementation Plan

## Overview

Build STMS as a phased vertical slice on AEM Cloud Service archetype: JCR data layer first, read UI, write servlets, app shell and styling, then hardening and documentation. Each phase has testable exit criteria before moving on.

| Phase | Focus | Status |
|---|---|---|
| 0 | Bootstrap (archetype, AGENTS.md, repoinit) | Complete |
| 1 | Data layer (`TicketRepository`, models, index) | Complete |
| 2 | Read UI (list, detail) | Complete |
| 3 | Write flows (create, edit, comment) | Complete |
| 4 | App shell + visual design | Complete |
| 5 | Tests + documentation + IT scaffold | Complete |
| 6 | Stretch (E2E, workflow, headless API) | Planned |

---

## Task Breakdown

### Phase 0 — Bootstrap

| Task | Module | Output |
|---|---|---|
| Configure Maven multi-module project | root | `pom.xml` |
| Add `AGENTS.md`, `.cursorrules` | root | AI/dev guidance |
| Repoinit + DAM path | `ui.config` | `RepositoryInitializer~stms.cfg.json` |
| Sample site structure | `ui.content` | `/content/stms`, `/conf/stms` |

### Phase 1 — Data layer

| Task | Module | Output |
|---|---|---|
| JCR schema + Sling Models | `core` | `TicketModel`, `TicketCommentModel`, enums |
| `TicketRepository` + impl | `core` | QueryBuilder, create/update/comment |
| Service user mapping | `ui.config` | `stms-ticket-write` → `stms-ticket-service` |
| Oak index | `ui.config` | `stms-ticket-index` |
| Unit tests | `core` | `TicketRepositoryImpl*Test`, `TicketModelTest` |

### Phase 2 — Read UI

| Task | Module | Output |
|---|---|---|
| `TicketListModel` + `ticketlist` | `core`, `ui.apps` | Filters, sort, HTL |
| `TicketDetailModel` + `ticketdetail` | `core`, `ui.apps` | Detail by `ticketId` |
| Sample pages | `ui.content` | tickets list, ticket-detail |

### Phase 3 — Write flows

| Task | Module | Output |
|---|---|---|
| DTOs + validation | `core` | `TicketCreateRequest`, `*Result` types |
| Servlets | `core` | `/bin/stms/ticket/create|update|comment` |
| Form components | `ui.apps` | `ticketcreate`, `ticketedit`, `ticketcomments` |
| Servlet tests | `core` | `*ServletTest` |

### Phase 4 — Shell & design

| Task | Module | Output |
|---|---|---|
| `AppShellModel` + `appshell` | `core`, `ui.apps` | Sidebar, top bar |
| Design tokens | `ui.apps` | `clientlib-base/css/tokens.css` |
| Wrap pages in appshell | `ui.content` | All ticket pages |

### Phase 5 — Hardening

| Task | Output |
|---|---|
| Full unit suite | 18+ tests in `core` |
| `TicketCreateIT` | `it.tests` integration scaffold |
| Assessment docs | Root `*.md`, `ai-prompts/history/` |
| Dispatcher validate | `validate.sh src` |

---

## Milestones

| Milestone | Date (target) | Exit criteria |
|---|---|---|
| M1 — Schema + repository | 2026-08-31 | `mvn test -pl core`; plan todos complete |
| M2 — List + detail UI | 2026-08-31 | Pages render sample tickets |
| M3 — Full CRUD + comments | 2026-09-01 | End-to-end author smoke pass |
| M4 — App shell + styling | 2026-09-01 | Consistent nav and tokens |
| M5 — Assessment package | 2026-09-01 | Docs + prompt history + IT compile |

---

## AI Usage Plan

| Phase | Cursor mode | Skills / context |
|---|---|---|
| Requirements | Ask | `STMS-propmts-history.md`, gap-analysis prompts |
| Design | Plan | JCR schema plan file; `design-notes.md` draft |
| Implementation | Agent | `create-component`, reference `TicketCreateServlet` |
| Testing | Agent | "Match `TicketRepositoryImplCreateTest` style" |
| Debugging | Agent + MCP | `debugging-notes.md`, AEM MCP logs |
| Review | Ask | `code-review-notes.md` checklist |
| Documentation | Agent | Restructure per assessment template |

**Rules I followed:**

- Attach `@` reference files and module scope in every implementation prompt
- Run `mvn test -pl core` after Java changes
- Do not let AI edit plan files during implementation
- Log prompts with accept/reject in `ai-prompts/history/`

---

## Risks

| Risk | Impact | Likelihood |
|---|---|---|
| Service user / repoinit not deployed | Writes fail | Medium |
| QueryBuilder empty list (wrong resourceType) | UI appears broken | Medium |
| Over-reliance on `.agents/skills` without review | Generic code | Medium |
| Documentation written retrospectively | Weak authenticity signal | High (addressed in prompt history) |
| No E2E tests in MVP | Regression on UI only caught manually | Medium |
| CSRF token missing on forms | POST 403 | Low |

---

## Mitigation

| Risk | Mitigation |
|---|---|
| Service user | Deploy `ui.config` with full package; document in `debugging-notes.md` |
| Empty list | Enforce `sling:resourceType` on create; unit test QueryBuilder predicates |
| Skill over-reliance | Explicit "match existing file X" prompts; manual diff review |
| Doc authenticity | `ai-prompts/history/` with iteration, rejections, MCP/debug evidence |
| No E2E | `TicketCreateIT` + manual smoke checklist in `acceptance-criteria.md` |
| CSRF | Granite token in all HTL forms; servlet tests for POST-only |

---

## Deploy commands

```bash
mvn clean install -PautoInstallSinglePackage
mvn clean install -pl core -PautoInstallBundle
mvn clean verify -pl it.tests -Plocal
```
