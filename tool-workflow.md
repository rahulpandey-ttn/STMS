# STMS Tool Workflow — AI Workflow Foundation

Use this file to describe **how AI is used across the STMS delivery lifecycle** and to map a user request to the correct repo layout, modules, skills, and runtime tools **before** editing files or running commands.

**STMS** is an AEM as a Cloud Service project (Java 21, Maven, Webpack frontend). Primary domain: **support ticket management** with an app shell, JCR-backed tickets, and Sling servlets.

Project guidance lives in `AGENTS.md` at the repo root. Java version for Cloud Manager is defined in `.cloudmanager/java-version` (21).

---

## Coverage checklist

| Topic | Section |
|---|---|
| Primary AI tool used | [Primary AI tool](#primary-ai-tool) |
| Providing project context | [Project context](#providing-project-context-to-the-ai-tool) |
| Requirement analysis | [Requirement analysis](#requirement-analysis) |
| Planning and design | [Planning and design](#planning-and-design) |
| Code generation | [Code generation](#code-generation) |
| Validating AI-generated code | [Validation](#validating-ai-generated-code) |
| Testing with AI | [Testing](#testing-with-ai) |
| Debugging with AI | [Debugging](#debugging-with-ai) |
| Code review with AI | [Code review](#code-review-with-ai) |
| Information to avoid sharing | [Data boundaries](#information-to-avoid-sharing-with-ai-tools) |
| Reuse in a real project | [Reuse](#reusing-this-workflow-in-a-real-project) |

---

## AI development workflow

### Primary AI tool

**Cursor** (Agent mode) is the primary AI development tool for STMS.

| Capability | How it is used in STMS |
|---|---|
| **Agent mode** | End-to-end implementation: read codebase, edit files, run Maven/npm, deploy to local SDK |
| **Ask mode** | Architecture questions, code explanation, and review without file changes |
| **Plan mode** | Multi-step feature design before implementation (e.g. new ticket field across core + ui.apps) |
| **Skills** | Domain-specific playbooks under `.agents/skills/` (create-component, aem-workflow, dispatcher, code-assessment, aem-rde) |
| **Rules** | `AGENTS.md`, `.cursorrules`, and workspace rules enforce AEM Cloud Service conventions |
| **MCP — `user-aem-local-author`** | Runtime diagnostics against local AEM author: OSGi bundle health, request traces, log tail |
| **MCP — GitLab** | Issue/MR context when connected (optional; not required for local STMS work) |

Secondary tools (used alongside Cursor, not instead of it):

- **Local AEM SDK** (`localhost:4502`) — runtime verification after AI-generated deploys
- **Maven / npm** — compile, test, and package validation the agent runs after changes
- **Dispatcher SDK** — `dispatcher/bin/validate.sh` for config changes

---

### Providing project context to the AI tool

Context is layered so the agent understands STMS without repeating the full repo on every prompt.

**Always-on context (automatic)**

| Source | What the AI learns |
|---|---|
| `AGENTS.md` | Module layout, build commands, Adobe Cloud Service doc links |
| `.cursorrules` | Java 21, Sling Models, HTL, Granite UI, module separation |
| `.cursor/rules/` and user rules | Git safety, commit conventions, minimal diffs |
| Open files / @-mentions | Active file focus when the developer pins a path |

**Project-specific context (curated)**

| Source | When to use |
|---|---|
| **This file** (`tool-workflow.md`) | Routing: which module, skill, and files to touch for a task |
| `.res.local/documents/STMS-propmts.md` | Feature prompts and acceptance-style requirements |
| `.agents/skills/<skill>/SKILL.md` | Dispatcher, workflow, component creation, code-assessment patterns |
| `@` file or folder in chat | Point the agent at `TicketRepositoryImpl.java`, a component folder, etc. |
| Existing code as reference | "Match `TicketCreateServlet`" — agent reads sibling implementations |

**Runtime context (when AEM is running)**

| Source | When to use |
|---|---|
| MCP `user-aem-local-author` | Bundle not active, servlet 500, repoinit/ACL issues |
| Terminal output | Maven compile errors, test failures, deploy logs |

**Prompt pattern that works well**

```text
Implement [feature] in STMS.
Follow patterns in [existing file].
Touch only: core + ui.apps (+ ui.config if service user/index needed).
Run mvn test -pl core after changes.
```

---

### Requirement analysis

AI is used to turn informal asks into concrete, repo-aware requirements **before** coding.

**Steps**

1. **Start from a structured prompt** — use or adapt entries in `.res.local/documents/STMS-propmts.md` (e.g. ticket list with sort/filter, comment component, app shell).
2. **Ask the agent to gap-analyze** — "Does a component already exist for X?" (see prompt #4 in STMS-propmts: list tickets).
3. **Anchor to existing domain model** — reference `TicketRepository`, JCR paths under `/content/stms/tickets`, servlet endpoints, and enums (`TicketStatus`, `TicketPriority`).
4. **Clarify scope** — Agent mode should confirm: read-only vs write, which pages/components, author-only vs publish, need for new index or repoinit.
5. **Output a short requirement summary** — acceptance criteria the agent (or human) can verify later.

**Example (STMS ticket list)**

| Requirement | Detail |
|---|---|
| Display all tickets | `TicketListModel` + `ticketlist` HTL |
| Sort by creation time | `TicketSearchCriteria` / QueryBuilder in `TicketRepositoryImpl` |
| Filter by status, assignee | Dialog or query params → repository criteria |
| Create action on list | Link to `/content/stms/us/en/tickets/create-ticket` |

**Ask mode** is appropriate for requirement analysis when you want options and trade-offs without file edits.

---

### Planning and design

AI supports design **before** implementation, especially for cross-module features.

**Planning approaches**

| Approach | Use when |
|---|---|
| **Plan mode in Cursor** | New feature spans `core`, `ui.apps`, `ui.config`, and `ui.content` |
| **Workflow maps in this doc** | Known task type (new component, ticket field, dispatcher rule) — follow the numbered map |
| **`create-component` skill** | New AEM component with dialog, HTL, optional Sling Model |
| **Design prompts in STMS-propmts** | Foundation work (JCR schema, Sling Model architecture — prompts #2–3) |
| **Vertical-slice table** | Ticket changes: repository → servlet → model → HTL → tests (see [Workflow map #2](#2-ticket-feature-change-list-create-edit-detail-comments)) |

**Design outputs to request from AI**

- JCR node shape and `sling:resourceType` values
- OSGi service boundaries (`TicketRepository` vs servlets vs models)
- File list per module (minimal diff)
- Service-user / repoinit impact if writes are involved
- Test plan (which existing test classes to extend)

**Guardrail:** Plans should respect AEM Cloud Service constraints in [Guardrails](#guardrails-non-negotiable) — no `/libs` writes, no admin sessions for ticket persistence.

---

### Code generation

AI generates code **by extending existing STMS patterns**, not greenfield frameworks.

**Generation rules**

| Layer | Pattern to follow |
|---|---|
| Java services | `TicketRepository` / `TicketRepositoryImpl` — DS `@Component`, service-user subservice for writes |
| Servlets | `TicketCreateServlet` — `@SlingServletPaths`, JSON request/response, delegate to repository |
| Sling Models | `@Model`, `@OSGiService` for services, `adaptables = SlingHttpServletRequest.class` |
| HTL + dialogs | Existing `ticket*` components — Coral 3 Granite, component `clientlibs/` |
| OSGi config | `ui.config/.../osgiconfig/config/*.cfg.json` |
| Unit tests | `io.wcm.testing.aem-mock.junit5` — mirror `TicketRepositoryImplCreateTest` |

**How generation is triggered**

1. Requirement + reference file(s) in the prompt
2. Agent reads workflow map → identifies modules
3. Agent implements smallest correct diff across layers
4. Agent runs compile/test commands (see [Validation](#validating-ai-generated-code))

**Skills invoked for generation**

- `create-component` — new components
- `aem-workflow/*` — Granite workflow models/steps (not yet used in STMS code)
- `dispatcher/*` — dispatcher config changes

---

### Validating AI-generated code

AI-generated code is **not trusted until verified** by automated checks and manual spot-review.

**Automated validation (agent or developer runs)**

| Check | Command / tool | Pass criteria |
|---|---|---|
| Core unit tests | `mvn test -pl core` | All tests green; extend ticket tests when behavior changes |
| Full compile | `mvn clean install` | No compile/analyser failures |
| Single-module deploy | `mvn clean install -pl core -PautoInstallBundle` | Bundle active on author |
| Package deploy | `mvn clean install -pl ui.apps,core -PautoInstallPackage` | Components render, dialogs work |
| Full local deploy | `mvn clean install -PautoInstallSinglePackage` | End-to-end on SDK |
| Frontend | `cd ui.frontend && npm run dev` | Clientlibs generated under `ui.apps` |
| Dispatcher | `cd dispatcher && ./bin/validate.sh src` | No validation errors |
| AEM analyser | Included in `mvn install` | Cloud Service compatibility |

**Runtime validation (manual or MCP-assisted)**

| Check | How |
|---|---|
| Ticket CRUD | Create → list → detail → edit → comment on local author |
| Service user writes | Ticket nodes appear under `/content/stms/tickets` without admin session in servlet |
| OSGi health | MCP `diagnose-osgi-bundle` if `stms.core` not `Active` |
| Errors | MCP `logs` or `recent-requests` after failed servlet/page load |

**Human validation**

- Review `git diff` for scope creep, secrets, and convention drift
- Confirm only intended modules changed

---

### Testing with AI

| Test type | Location | AI role |
|---|---|---|
| **Unit (primary)** | `core/src/test/java/com/ttn/stms/core/tickets/` | Generate/update AEM Mock tests alongside repository, model, and servlet changes |
| **Integration** | `it.tests/` | Scaffold or extend HTTP-level tests against running AEM (Cloud Manager step) |
| **UI / E2E** | `ui.tests/test-module/` (Cypress) | Draft Cypress flows for ticket list, create, detail (run against local/pipeline AEM) |

**AI testing workflow**

1. After implementing a feature, ask: "Add unit tests matching `TicketRepositoryImplCreateTest` style."
2. Run `mvn test -pl core`; feed failures back to the agent for fixes.
3. For UI changes, verify in browser on `http://localhost:4502/content/stms/us/en/tickets.html`.
4. Optionally ask AI to draft a Cypress spec; developer runs `ui.tests` locally.

**Rule:** Behavior changes in `TicketRepositoryImpl` or servlets should include test updates in the same change set.

---

### Debugging with AI

| Symptom | AI + tool approach |
|---|---|
| Compile / test failure | Paste Maven output; agent fixes from stack trace |
| Bundle not active | MCP `diagnose-osgi-bundle` → missing import, DS unsatisfied reference |
| Servlet 4xx/5xx | MCP `recent-requests` → request path, status, exception |
| Repoinit / ACL / write failure | MCP `logs` filtered on `stms` or `RepositoryInitializer`; verify `ui.config` repoinit + service-user mapping |
| Query / empty list | Review `TicketRepositoryImpl` predicates and `stms-ticket-index` in `ui.config` |
| HTL / dialog issue | Agent reads component HTL + `_cq_dialog` + Sling Model; compare with working `ticketcreate` |
| Dispatcher | `validate.sh` output + `dispatcher` skill incident-response playbooks |

**Debug prompt pattern**

```text
Ticket create returns 500. Check TicketCreateServlet and TicketRepositoryImpl.
Use AEM MCP logs and recent-requests if needed.
```

---

### Code review with AI

| Review type | How |
|---|---|
| **Pre-commit diff review** | Ask mode or Agent: "Review my changes for AEM Cloud Service issues and STMS conventions." |
| **`code-assessment` skill** | Scan for inject-in-sling-model, unbounded queries, deprecated APIs, replication patterns |
| **Security review** | Workspace security rules: no hardcoded credentials, no `loginAdministrative`, service-user for writes |
| **Scope review** | Confirm diff touches only workflow-map modules; no unrelated refactors |
| **Bugbot / Security Review subagents** | Optional Cursor subagents on branch or uncommitted changes before MR |

**Review checklist (STMS-specific)**

- [ ] Writes use `stms-ticket-write` subservice, not request user's resolver
- [ ] New properties persisted in repository **and** exposed in model **and** HTL/dialog
- [ ] Unit tests updated
- [ ] No secrets or environment-specific URLs committed
- [ ] Dispatcher validated if `dispatcher/src` changed

---

### Information to avoid sharing with AI tools

Share **code and architecture**, not **secrets or sensitive operational data**.

| Do not share | Why |
|---|---|
| Production passwords, API keys, OAuth client secrets | Credential leakage; use env vars / Cloud Manager secrets |
| Real customer PII in prompts (names, emails, ticket content from prod) | Privacy / compliance |
| Production admin URLs with embedded tokens | Operational security |
| Full production log dumps with auth headers or session IDs | May contain secrets |
| Private Maven repo credentials | Keep in `settings.xml` locally, not in chat |
| Unredacted `pom.xml` deploy credentials if added | Local SDK defaults (`admin`/`admin`) are for dev only — do not reuse in prod narratives |

| Safe to share | Examples |
|---|---|
| Project source structure | Paths under `core/`, `ui.apps/`, this doc |
| Anonymized requirements | "Filter tickets by status Open/In Progress" |
| Stack traces from **local** SDK | Compile errors, local servlet exceptions |
| Public Adobe documentation links | Experience League URLs from `AGENTS.md` |

`.res.local/` is intended for local notes and prompts; keep it out of version control if it ever contains environment-specific or sensitive notes.

---

### Reusing this workflow in a real project

This workflow is **portable** to other AEM Cloud Service engagements with minimal adaptation.

**1. Bootstrap project context**

| Artifact | Purpose |
|---|---|
| `AGENTS.md` | Module catalog, build commands (use `ensure-agents-md` skill on new repos) |
| `.cursorrules` | Stack conventions (Java version, HTL, Core Components) |
| `.res.local/documents/tool-workflow.md` | Copy and replace STMS-specific tables (paths, domain model, servlets) |
| `.res.local/documents/<project>-prompts.md` | Requirement prompts per feature epic |

**2. Install domain skills**

Copy or install `.agents/skills/` subsets needed for the engagement: `create-component`, `dispatcher`, `aem-workflow`, `code-assessment`, `aem-rde`.

**3. Wire runtime MCP (optional)**

Connect `user-aem-local-author` (or equivalent) for local SDK diagnostics — same debug workflow as [Debugging](#debugging-with-ai).

**4. Adapt workflow maps**

Replace the **Ticket domain** section with the new project's domain (e.g. products, articles, forms). Keep the same **module routing** pattern:

- Java → `core`
- HTL/dialogs → `ui.apps`
- OSGi → `ui.config`
- Sample content → `ui.content`

**5. Standard delivery loop**

```text
Requirement (prompts.md)
  → Plan (workflow map + Plan mode)
  → Generate (Agent + skills + reference files)
  → Validate (mvn test/install, dispatcher validate, SDK smoke test)
  → Debug (MCP logs / Maven output)
  → Review (diff + code-assessment)
  → Commit / MR (human-owned)
```

**6. What transfers unchanged**

- Cursor Agent / Ask / Plan usage model
- Maven/npm validation gates
- MCP-based local AEM diagnostics
- Skills-based domain playbooks
- Security boundaries ([Information to avoid sharing](#information-to-avoid-sharing-with-ai-tools))
- Cloud Service guardrails (no `/libs` writes, service users, pipeline deploy)

**STMS as reference implementation**

This repository demonstrates the full loop for a **ticket management** vertical: JCR schema, repository + servlets, six components, app shell, repoinit, Oak index, and unit tests — usable as a template when scoping similar CRUD features on AEM.

---

## STMS technical reference

The sections below map STMS repo layout, domain model, and task-specific file paths for AI agents and developers.

---

## Repo Layout (Layout B — Dispatcher Subproject)

```text
stms/
├── core/                    # OSGi bundle (Java backend)
├── ui.apps/                 # Components, clientlibs, dialogs (HTL)
├── ui.apps.structure/       # Repository structure package
├── ui.config/               # OSGi configs, repoinit, Oak indexes
├── ui.content/              # Sample pages, templates, conf
├── ui.frontend/             # Webpack → clientlibs in ui.apps
├── dispatcher/src/          # Cloud Dispatcher config
├── it.tests/                # Integration tests (AEM Testing Clients)
├── ui.tests/                # Cypress E2E tests
├── all/                     # Aggregator content package
└── .agents/skills/          # Project-local AI skills
```

| Concern | Source root |
|---|---|
| Java / OSGi | `core/src/main/java/com/ttn/stms/` |
| HTL / dialogs / clientlibs | `ui.apps/src/main/content/jcr_root/apps/stms/` |
| OSGi / repoinit / indexes | `ui.config/src/main/content/jcr_root/` |
| Pages / templates / conf | `ui.content/src/main/content/jcr_root/` |
| Frontend build | `ui.frontend/` |
| Dispatcher | `dispatcher/src/` |

---

## Application Identity

| Item | Value |
|---|---|
| Maven `groupId` | `com.ttn.stms` |
| App namespace | `stms` |
| App root | `/apps/stms` |
| Conf root | `/conf/stms` |
| Content root | `/content/stms` |
| DAM root | `/content/dam/stms` |
| Component group | `STMS` |
| Core bundle artifact | `stms.core` |
| AEM SDK API (pom) | `2026.8.27830.20260820T160150Z-260700` |
| Core WCM Components | `2.28.0` |

---

## Domain Model — Tickets

### JCR paths

| Path | Purpose |
|---|---|
| `/content/stms/tickets` | Ticket storage root (`TicketRepository.TICKETS_ROOT`) |
| `/content/stms/tickets/<ticketId>` | Individual ticket node (`stms/tickets/ticket`) |
| `/content/stms/tickets/<ticketId>/comments` | Comments container (`stms/tickets/comments`) |
| `/content/stms/tickets/<ticketId>/comments/<commentId>` | Comment node (`stms/tickets/comment`) |

### Author pages (sample content)

| Page | Path |
|---|---|
| Ticket list | `/content/stms/us/en/tickets` |
| Create ticket | `/content/stms/us/en/tickets/create-ticket` |
| Edit ticket | `/content/stms/us/en/tickets/edit-ticket` |
| Ticket detail | `/content/stms/us/en/ticket-detail` |

### Servlet API (`/bin/stms/ticket/*`)

| Endpoint | Servlet | Backend |
|---|---|---|
| `POST /bin/stms/ticket/create` | `TicketCreateServlet` | `TicketRepository.createTicket()` |
| `POST /bin/stms/ticket/comment` | `TicketCommentServlet` | `TicketRepository.addComment()` |
| `POST /bin/stms/ticket/update` | `TicketEditServlet` | `TicketRepository.updateTicket()` |

Writes use service subservice `stms-ticket-write` → service user `stms-ticket-service` (repoinit + service-user mapping in `ui.config`).

### Components ↔ models

| Component (`stms/components/*`) | Sling Model | Notes |
|---|---|---|
| `appshell` | `AppShellModel` | Sidebar/top bar; wires nav to ticket pages |
| `ticketlist` | `TicketListModel` | Lists tickets via `TicketRepository` |
| `ticketcreate` | `TicketCreateModel` | Form → create servlet |
| `ticketedit` | `TicketEditModel` | Form → update servlet |
| `ticketdetail` | `TicketDetailModel` | Single ticket view |
| `ticketcomments` | `TicketCommentsModel` | Comments + add-comment servlet |

Content structure types (not components): `stms/tickets/ticket`, `stms/tickets/comments`, `stms/tickets/comment`.

### Enums

- `TicketStatus`, `TicketPriority` in `core/.../tickets/enums/`

### Search / indexing

- QueryBuilder used in `TicketRepositoryImpl`
- Oak Lucene index: `ui.config/.../_oak_index/stms-ticket-index/` (indexes `sling:resourceType`, `status`, `assignee`, `createdDate`)

---

## Workflow Maps

### 1) New AEM component (general)

**Skill:** `create-component`

Usually touch:

- `ui.apps/.../apps/stms/components/<name>/` — `.content.xml`, HTL, `_cq_dialog/`, optional `clientlibs/`
- `core/.../models/<Name>Model.java` — if backend logic needed
- `core/src/test/java/...` — Sling Model unit test (AEM Mock)
- `ui.content/` — only if sample page placement is required

Runtime proof:

- Component appears in editor
- Dialog saves properties
- HTL renders expected output

Deploy:

```bash
mvn clean install -pl ui.apps,core -PautoInstallPackage   # ui.apps + embedded core
# or full package:
mvn clean install -PautoInstallSinglePackage
```

---

### 2) Ticket feature change (list, create, edit, detail, comments)

**Start here** — read the vertical slice before editing:

| Layer | Key files |
|---|---|
| Repository / business logic | `core/.../tickets/services/TicketRepository.java`, `.../impl/TicketRepositoryImpl.java` |
| Request DTOs | `TicketCreateRequest`, `TicketUpdateRequest`, `TicketCommentCreateRequest`, `TicketSearchCriteria` |
| Servlets | `core/.../tickets/servlets/Ticket*Servlet.java` |
| Models | `core/.../tickets/models/Ticket*Model.java` |
| HTL + JS | `ui.apps/.../components/ticket*/` |
| OSGi / ACL | `ui.config/.../osgiconfig/config/` (repoinit, service-user mapping) |
| Sample pages | `ui.content/.../content/stms/us/en/tickets/` |
| Tests | `core/src/test/java/com/ttn/stms/core/tickets/` |

**Decision tree:**

- **Read-only / display logic** → Sling Model + HTL (`core` + `ui.apps`)
- **Create / update / comment persistence** → `TicketRepositoryImpl` + servlet (`core`); verify service user ACL
- **New filter or sort on list** → `TicketSearchCriteria`, `TicketRepositoryImpl.findTickets`, `TicketListModel`, `ticketlist` HTL/JS
- **New ticket field** → Model properties, repository write/read, dialog, HTL, and tests
- **New index requirement** → `ui.config/.../_oak_index/stms-ticket-index/`

Runtime proof:

- Create ticket via UI → node under `/content/stms/tickets/TICKET-*`
- List reflects new ticket; detail/edit/comment flows work
- Servlet returns expected JSON on success and validation errors

Deploy bundle only (faster iteration):

```bash
mvn clean install -pl core -PautoInstallBundle
```

---

### 3) App shell / navigation change

Usually touch:

- `core/.../shell/models/AppShellModel.java`, `AppShellNavItem.java`
- `ui.apps/.../components/appshell/` (HTL, CSS, JS, dialog)
- `ui.content/` pages that embed `stms/components/appshell` with `ticketsListPage`, `createTicketPage` dialog props

Runtime proof:

- Nav highlights active page
- Links resolve to correct ticket pages

---

### 4) OSGi config, repoinit, service users

**Module:** `ui.config` only (unless Java constants reference subservice names)

Key files:

- `org.apache.sling.jcr.repoinit.RepositoryInitializer~stms.cfg.json` — DAM folder, `stms-ticket-service`, ACL on `/content/stms/tickets`
- `org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended~stms-tickets.cfg.json` — maps `stms.core:stms-ticket-write`
- Runmode-specific configs under `config.author/`, `config.publish/`

Runtime proof:

- Repoinit applied (service user exists, ACL correct)
- `TicketRepositoryImpl` write operations succeed without admin session

---

### 5) Frontend / global styles / site clientlib

**Module:** `ui.frontend` → outputs to `ui.apps/.../clientlibs/clientlib-site/`

```bash
cd ui.frontend && npm run dev      # build + clientlib
cd ui.frontend && npm start        # webpack dev server
cd ui.frontend && npm run watch    # watch + aemsync to ui.apps
```

Component-scoped CSS/JS lives in each component's `clientlibs/` under `ui.apps` (ticket components use this pattern).

Shared design tokens: `ui.apps/.../clientlibs/clientlib-base/css/` (`tokens.css`, `base.css`, etc.).

---

### 6) Dispatcher / caching / security

**Skill:** `dispatcher` (or sub-skills: `config-authoring`, `security-hardening`, `performance-tuning`, `incident-response`)

Dispatcher source root: `dispatcher/src`

Foundation reference: `.agents/skills/dispatcher/*/references/dispatcher-foundation/repo-layout-workflows.md`

Validate locally:

```bash
cd dispatcher && ./bin/validate.sh src
```

---

### 7) AEM Granite Workflow (approval, launchers, process steps)

**Note:** STMS has **no custom Granite Workflow** code today. For workflow tasks, use the `aem-workflow` skill family:

| Intent | Skill |
|---|---|
| Model design | `aem-workflow/workflow-model-design` |
| Java process steps | `aem-workflow/workflow-development` |
| Trigger from code | `aem-workflow/workflow-triggering` |
| Launchers | `aem-workflow/workflow-launchers` |
| Stuck / failed workflows | `aem-workflow/workflow-debugging` |
| Incident triage | `aem-workflow/workflow-triaging` |
| Broad / lifecycle | `aem-workflow/workflow-orchestrator` |

Always load `workflow-foundation` references from the orchestrator skill first.

---

### 8) Code quality / Cloud Service compliance

**Skill:** `code-assessment` (and pattern-specific sub-skills)

Common patterns for this codebase:

- `inject-in-sling-model` — `@Reference` → `@OSGiService` in Sling Models
- `unbounded-query` — review `TicketRepositoryImpl` QueryBuilder usage
- `resource-change-listener` — `SimpleResourceListener`
- `scheduler` — `SimpleScheduledTask`
- `replication` — content distribution changes

Git workflow for autofix runs: `.agents/skills/code-assessment/references/git-workflow.md`

---

### 9) Local RDE / hot deploy

**Skill:** `aem-rde`

Typical flow:

```bash
mvn -pl core -am clean install
aio aem rde install core/target/stms.core-*.jar
aio aem rde install ui.apps/target/stms.ui.apps-*.zip
```

See `.agents/skills/aem-rde/references/workflows.md` for full recipes.

---

### 10) Testing

| Type | Location | Command |
|---|---|---|
| Unit (core) | `core/src/test/java/` | `mvn test -pl core` |
| Integration | `it.tests/` | Against running AEM (Cloud Manager step) |
| UI (Cypress) | `ui.tests/test-module/` | Against running AEM (Cloud Manager step) |

Ticket tests already exist under `core/src/test/java/com/ttn/stms/core/tickets/` — extend these when changing ticket behavior.

---

## MCP Tools (local AEM author)

Namespace: `user-aem-local-author`

| Tool | Use when |
|---|---|
| `diagnose-osgi-bundle` | Bundle not active, DS unsatisfied, ticket writes failing |
| `recent-requests` | Servlet or page errors after UI/API change |
| `logs` | Stack traces, repoinit failures, QueryBuilder issues |

These tools inspect the **running AEM instance**, not the local filesystem.

---

## Build & Deploy Quick Reference

| Goal | Command |
|---|---|
| Full build | `mvn clean install` |
| Deploy all to local author | `mvn clean install -PautoInstallSinglePackage` |
| Deploy single package | `mvn clean install -pl <module> -PautoInstallPackage` |
| Deploy OSGi bundle only | `mvn clean install -pl core -PautoInstallBundle` |
| Frontend build | `cd ui.frontend && npm run dev` |
| Dispatcher validate | `cd dispatcher && ./bin/validate.sh src` |

Local defaults (root `pom.xml`): author `localhost:4502`, publish `localhost:4503`.

Production deploy: **Cloud Manager Full Stack Pipeline** only — no Package Manager in cloud.

---

## Guardrails (non-negotiable)

| Rule | Detail |
|---|---|
| `/libs` is immutable | Overlay in `/apps` or configure in `/conf` |
| Service-user writes | Ticket mutations via `stms-ticket-write` subservice — never `loginAdministrative` |
| Module separation | Java → `core`; HTL/dialogs → `ui.apps`; OSGi JSON → `ui.config`; sample content → `ui.content` |
| Core Components | Prefer `sling:resourceSuperType` over reimplementing WCM Core |
| HTL + Granite | Dialogs use Coral 3 / Granite UI structures |
| Cloud Service APIs | Use AEM SDK API; avoid deprecated 6.x-only APIs |
| Tests | Update `core` unit tests when ticket/repository behavior changes |
| Dispatcher | Run `validate.sh` after dispatcher edits |

---

## File-Family Heuristics

| User mentions… | Start in… |
|---|---|
| Ticket, comment, assignee, status | `core/.../tickets/` + matching `ui.apps/.../ticket*` component |
| Sidebar, nav, app chrome | `core/.../shell/` + `ui.apps/.../appshell` |
| Dialog, HTL, component | `ui.apps/.../components/` |
| OSGi, repoinit, index, CORS, logging | `ui.config/` |
| Page, template, policy | `ui.content/` + `conf/stms/settings/wcm/` |
| Styles, webpack, site JS | `ui.frontend/` or component `clientlibs/` |
| Cache, filter, vhost, farm | `dispatcher/src/` |
| Workflow model / launcher | No STMS code yet → `aem-workflow` skill |
| Dependency / SDK upgrade | Root `pom.xml` + `code-assessment/outdated-dependencies` |

---

## End-to-End Pattern: Add a field to tickets

1. Add property to `TicketModel` and persistence in `TicketRepositoryImpl` (create + update paths).
2. Extend `TicketCreateRequest` / `TicketUpdateRequest` and servlet validation if needed.
3. Update component dialogs and HTL (`ticketcreate`, `ticketedit`, `ticketdetail`, `ticketlist` as applicable).
4. Update JS in component clientlibs if the field is form-driven.
5. Add/adjust unit tests in `core/src/test/java/.../tickets/`.
6. Deploy: `mvn clean install -pl core,ui.apps -PautoInstallPackage`.
7. Verify: create ticket → field persisted → visible on list and detail.

---

## Related project docs

- `AGENTS.md` — module overview, build commands, Adobe docs links
- `.res.local/documents/STMS-propmts.md` — feature and design prompts used for requirement analysis
- `.agents/skills/` — AEM, dispatcher, migration, code-assessment, create-component skills
- `.cursorrules` — Java 21, Sling Models, HTL, Granite UI conventions
