# Candidate Information

**Name:** Rahul Pandey  
**Role:** AEM Full-Stack Developer  
**Primary Technology Stack:** AEM as a Cloud Service, Java 21, Sling Models, HTL, Maven, Webpack, JCR  

**Primary AI Tool Used:** Cursor (Agent, Ask, Plan modes) with `.agents/skills/` and AEM MCP (`user-aem-local-author`)  
**Project Option Selected:** STMS — Support Ticket Management System (AEM Cloud Service)  

**Assessment Start Date:** 2026-08-30  
**Submission Date:** 2026-09-01  

---

## Project Summary

STMS is a support ticket management application built on AEM as a Cloud Service. Tickets are stored as JCR nodes under `/content/stms/tickets`, rendered through six AEM components (app shell, list, create, detail, edit, comments), and mutated via OSGi `TicketRepository` and Sling servlets at `/bin/stms/ticket/*`.

Delivered capabilities:

- Ticket CRUD (create, read, update) and comments
- List with sort and filter (status, assignee, priority, creator)
- Service-user writes (`stms-ticket-service` / `stms-ticket-write`)
- Unit tests (18 classes) and integration test scaffold (`TicketCreateIT`)
- Cloud Service patterns: repoinit, Oak index, module separation (`core`, `ui.apps`, `ui.config`, `ui.content`)

---

## Tools Used

| Tool | Purpose |
|---|---|
| **Cursor** | Primary IDE agent — planning, implementation, debugging, documentation |
| **AEM Cloud SDK** | Local author (`localhost:4502`) runtime verification |
| **Maven** | Build, test, package deploy (`-PautoInstallSinglePackage`) |
| **AEM MCP** | Local log tail, OSGi bundle diagnosis, recent requests |
| **`.agents/skills/`** | Domain playbooks (`create-component`, `aem-workflow`, `code-assessment`) |
| **JUnit 5 + AEM Mock** | Unit tests in `core` |
| **AEM Testing Clients** | Integration tests in `it.tests` |
| **Dispatcher SDK** | `dispatcher/bin/validate.sh` |

---

## Setup Summary

### Prerequisites

- Java 21 (see `.cloudmanager/java-version`)
- AEM as a Cloud Service SDK (author on port 4502)
- Maven 3.x, Node.js (for `ui.frontend` optional)

### Build and deploy

```bash
mvn clean install -PautoInstallSinglePackage
```

### Smoke URLs (author)

| Page | URL |
|---|---|
| Ticket list | `http://localhost:4502/content/stms/us/en/tickets.html` |
| Create ticket | `http://localhost:4502/content/stms/us/en/tickets/create-ticket.html` |
| Ticket detail | `http://localhost:4502/content/stms/us/en/ticket-detail.html?ticketId=TICKET-0001` |

### Run tests

```bash
mvn test -pl core
mvn clean verify -pl it.tests -Plocal   # requires running AEM + deployed packages
```

### Key documentation

| File | Purpose |
|---|---|
| `requirements-analysis.md` | Requirements and assumptions |
| `implementation-plan.md` | Phased delivery plan |
| `design-notes.md` | Architecture |
| `api-contract.md` | Servlet API |
| `data-model.md` | JCR schema (supplementary) |
| `ui-flow.md` | User journeys (supplementary) |
| `ai-prompts/history/` | Prompt history by activity |
| `debugging-notes.md` | Real issues and fixes |
| `reflection.md` | Self-assessment |
