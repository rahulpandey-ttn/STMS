# Implementation Plan — STMS

Phased delivery plan for the Support Ticket Management System on AEM Cloud Service.

---

## Overview

| Phase | Focus | Status |
|---|---|---|
| **Phase 0** | Project bootstrap | Complete |
| **Phase 1** | Data layer & repository | Complete |
| **Phase 2** | Read components (list, detail) | Complete |
| **Phase 3** | Write flows (create, edit, comment) | Complete |
| **Phase 4** | App shell & visual design | Complete |
| **Phase 5** | Hardening & tests | Complete |
| **Phase 6** | Future enhancements | Planned |

---

## Phase 0 — Project bootstrap

**Goal:** Establish AEM Cloud Service archetype project with module structure and AI guidance.

| Task | Module | Output |
|---|---|---|
| Generate / configure Maven multi-module project | root | `pom.xml`, modules |
| Create `AGENTS.md` and `.cursorrules` | root | AI + developer conventions |
| Configure repoinit for DAM and service user | `ui.config` | `RepositoryInitializer~stms.cfg.json` |
| Sample site and templates | `ui.content` | `/content/stms`, `/conf/stms` |

**Exit criteria:** `mvn clean install` succeeds; package installs on local SDK.

---

## Phase 1 — Data layer & repository

**Goal:** JCR schema, Sling Models, OSGi repository, search index.

| Step | Task | Files |
|---|---|---|
| 1.1 | Define ticket JCR structure | `TicketModel`, `TicketCommentModel`, `TicketCommentsContainerModel` |
| 1.2 | Define enums | `TicketStatus`, `TicketPriority` |
| 1.3 | Implement `TicketRepository` | `TicketRepository.java`, `TicketRepositoryImpl.java` |
| 1.4 | Service-user mapping | `ServiceUserMapperImpl.amended~stms-tickets.cfg.json` |
| 1.5 | Oak index for queries | `_oak_index/stms-ticket-index/` |
| 1.6 | Unit tests | `TicketRepositoryImpl*Test.java`, `TicketModelTest.java` |

**Exit criteria:** Create/read tickets via repository tests; QueryBuilder returns filtered results.

---

## Phase 2 — Read components

**Goal:** List and detail views without write servlets.

| Step | Task | Files |
|---|---|---|
| 2.1 | `TicketListModel` with filters/sort | `core/.../TicketListModel.java` |
| 2.2 | `ticketlist` HTL + dialog | `ui.apps/.../ticketlist/` |
| 2.3 | `TicketDetailModel` | `core/.../TicketDetailModel.java` |
| 2.4 | `ticketdetail` HTL + dialog | `ui.apps/.../ticketdetail/` |
| 2.5 | Sample pages | `ui.content/.../tickets/`, `ticket-detail/` |
| 2.6 | Unit tests | `TicketListModelTest`, `TicketDetailModelTest` |

**Exit criteria:** AC-2 and AC-3 pass on local author.

---

## Phase 3 — Write flows

**Goal:** Create, update, and comment via servlets + forms.

| Step | Task | Files |
|---|---|---|
| 3.1 | DTOs and result types | `TicketCreateRequest`, `TicketUpdateRequest`, `TicketCommentCreateRequest`, `*Result` |
| 3.2 | Extend repository writes | `createTicket`, `updateTicket`, `addComment` in `TicketRepositoryImpl` |
| 3.3 | Servlets | `TicketCreateServlet`, `TicketEditServlet`, `TicketCommentServlet` |
| 3.4 | Form components | `ticketcreate`, `ticketedit`, `ticketcomments` |
| 3.5 | Edit page content | `ui.content/.../edit-ticket/` |
| 3.6 | Servlet + repository tests | `*ServletTest`, `TicketRepositoryImplCreateTest`, etc. |

**Exit criteria:** AC-1, AC-4, AC-5, AC-6 pass end-to-end on local author.

---

## Phase 4 — App shell & visual design

**Goal:** Consistent navigation and styling.

| Step | Task | Files |
|---|---|---|
| 4.1 | `AppShellModel` + nav items | `core/.../shell/` |
| 4.2 | `appshell` component | `ui.apps/.../appshell/` |
| 4.3 | Design tokens & base CSS | `clientlib-base/css/tokens.css`, etc. |
| 4.4 | Component clientlibs | `ticket*/clientlibs/` |
| 4.5 | Wrap ticket pages in appshell | `ui.content` page roots |

**Exit criteria:** AC-7 pass; visual consistency across ticket pages.

---

## Phase 5 — Hardening & tests

**Goal:** Quality gates and documentation.

| Step | Task | Output |
|---|---|---|
| 5.1 | Complete unit test suite | 18+ tests in `core/src/test` |
| 5.2 | Validation rules enforced | Title/comment length, enum checks |
| 5.3 | AI workflow documentation | `.res.local/documents/tool-workflow.md` |
| 5.4 | Project documentation | Root `*.md` files |
| 5.5 | Dispatcher validation | `dispatcher/bin/validate.sh src` |

**Exit criteria:** AC-9 pass; docs complete.

---

## Phase 6 — Future enhancements (planned)

| Item | Effort | Dependencies |
|---|---|---|
| Cypress E2E for ticket flows | Medium | `ui.tests` against running AEM |
| Integration tests (HTTP) | Medium | `it.tests` |
| Assignee picker (user/group dialog) | Medium | Granite user picker |
| Granite Workflow for approvals | Large | `aem-workflow` skill |
| Headless JSON API | Medium | New Sling servlets or GraphQL |
| Ticket delete / archive | Small | Repository + UI |

---

## Module dependency graph

```text
ui.content  ──depends on──►  ui.apps  ──embeds──►  core
ui.config   ──standalone OSGi configs
ui.frontend ──build output──►  ui.apps (clientlib-site)
all         ──aggregates──►  ui.apps, ui.config, ui.content, core
dispatcher  ──independent──►  Cloud Manager dispatcher pipeline
```

---

## Deploy sequence (local SDK)

```bash
# Full deploy
mvn clean install -PautoInstallSinglePackage

# Iterative backend
mvn clean install -pl core -PautoInstallBundle

# UI only
mvn clean install -pl ui.apps -PautoInstallPackage
```

---

## Risk register

| Risk | Mitigation |
|---|---|
| Service user ACL missing | Repoinit in `ui.config`; verify via MCP `diagnose-osgi-bundle` |
| QueryBuilder performance | Oak index `stms-ticket-index` |
| CSRF failures on forms | Include `:cq_csrf_token` in HTL forms |
| Cloud Service API drift | Pin AEM SDK API in root `pom.xml`; run analyser |
