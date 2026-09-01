# Candidate Information

## Profile

| Field | Value |
|---|---|
| **Project** | STMS — Support Ticket Management System |
| **Platform** | Adobe Experience Manager as a Cloud Service |
| **Organization** | TTN (`com.ttn.stms`) |
| **Role** | AEM Full-Stack Developer |
| **Primary AI tool** | Cursor (Agent, Ask, and Plan modes) |

> **Note:** Replace placeholder fields below with your personal details before submission.

| Field | Value |
|---|---|
| **Name** | _[Your name]_ |
| **Email** | _[Your email]_ |
| **Date** | _[Submission date]_ |

---

## Technical background

- **Backend:** Java 21, OSGi Declarative Services, Sling Models, Sling Servlets, QueryBuilder
- **Frontend:** HTL (Sightly), Granite/Coral UI dialogs, component clientlibs, Webpack
- **AEM:** Editable templates, Core WCM Components, repoinit, service users, Oak indexes
- **Build & deploy:** Maven, AEM SDK local Quickstart, Cloud Manager pipelines
- **Testing:** JUnit 5, WCM.io AEM Mocks, Cypress (ui.tests), AEM Testing Clients (it.tests)
- **Tooling:** Cursor AI, AEM MCP (`user-aem-local-author`), Dispatcher SDK validation

---

## STMS project summary

Built a support ticket management application on AEM Cloud Service featuring:

- JCR-backed ticket storage under `/content/stms/tickets`
- CRUD operations via OSGi `TicketRepository` and Sling servlets (`/bin/stms/ticket/*`)
- Six AEM components: app shell, ticket list, create, detail, edit, and comments
- Service-user writes (`stms-ticket-service` / `stms-ticket-write` subservice)
- Unit test coverage for repository, models, and servlets

---

## AI-assisted delivery approach

| Phase | Tool / artifact |
|---|---|
| Requirements | Cursor + `.res.local/documents/STMS-propmts.md` |
| Planning | Cursor Plan mode + `implementation-plan.md` |
| Implementation | Cursor Agent mode + `.agents/skills/` |
| Validation | `mvn test -pl core`, local AEM SDK, AEM MCP diagnostics |
| Documentation | Project root docs (`requirements-analysis.md`, `api-contract.md`, etc.) |

See `.res.local/documents/tool-workflow.md` for the full AI workflow foundation.

---

## Repository references

| Document | Purpose |
|---|---|
| `AGENTS.md` | Project module guide for AI and developers |
| `requirements-analysis.md` | Functional and non-functional requirements |
| `acceptance-criteria.md` | Testable acceptance criteria |
| `implementation-plan.md` | Phased delivery plan |
| `design-notes.md` | Architecture and design decisions |
| `api-contract.md` | Servlet API specification |
| `data-model.md` | JCR schema and Sling Models |
| `ui-flow.md` | End-user page flows |
| `test-strategy.md` | Testing approach and coverage |
