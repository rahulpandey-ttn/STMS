# AI Prompts — Planning

Reusable prompts for **requirement analysis and implementation planning** on the STMS AEM Cloud Service project.

**Tool:** Cursor (Plan or Agent mode)  
**Read first:** `AGENTS.md`, `requirements-analysis.md`, `implementation-plan.md`

---

## When to use

- Starting a new feature (e.g. ticket attachments, assignee picker)
- Breaking a large ask into phased, module-scoped work
- Confirming what already exists before building
- Estimating files and modules to touch

---

## Context to attach

| Artifact | Why |
|---|---|
| `requirements-analysis.md` | Baseline functional requirements |
| `implementation-plan.md` | Existing phases and module graph |
| `data-model.md` | JCR and repository boundaries |
| `ai-prompts/STMS-propmts-history.md` | Prior feature prompts |
| `@core/.../tickets/` or `@ui.apps/.../ticket*` | Current implementation |

---

## Prompt 1 — Gap analysis (does it exist?)

```text
In the STMS AEM project, confirm whether we already have implementation for:
[describe feature — e.g. "a component that lists all tickets with sort and filter"].

Search core (Sling Models, TicketRepository) and ui.apps (components under apps/stms).
Reply with: exists / partial / missing, file paths, and what would still need to be built.
Do not make code changes.
```

**STMS example:** Prompt #4 from STMS history — list tickets component.

---

## Prompt 2 — Requirement breakdown

```text
Break down this requirement for STMS (AEM Cloud Service, Java 21):

"[paste user story or feature request]"

Produce:
1. Functional requirements (numbered)
2. Non-functional requirements (security, Cloud Service, performance)
3. Out of scope items
4. Assumptions
5. Mapping to modules: core | ui.apps | ui.config | ui.content | ui.frontend | dispatcher

Follow patterns in requirements-analysis.md. Keep ticket domain conventions:
- storage: /content/stms/tickets
- writes: TicketRepository + stms-ticket-write service user
```

---

## Prompt 3 — Phased implementation plan

```text
Create a phased implementation plan for STMS:

Feature: [feature name]
Requirement summary: [1–3 sentences]

For each phase include:
- Goal
- Tasks with module and key files
- Exit criteria (testable)
- Dependencies on prior phases

Align with implementation-plan.md style. Prefer extending TicketRepository,
existing ticket* components, and ui.config repoinit over new patterns.
```

---

## Prompt 4 — Scope and risk check

```text
Review this planned change for STMS and list:
- Modules that must change
- Cloud Service risks (/libs writes, admin sessions, deprecated APIs)
- Service-user / repoinit impact
- Oak index or QueryBuilder impact
- Test classes to add or update

Plan: [paste plan or bullet list]

Output: risks (High/Medium/Low) and mitigations. No code yet.
```

---

## Prompt 5 — Acceptance criteria draft

```text
Write acceptance criteria for STMS feature "[feature name]" in Given/When/Then format.

Cover:
- Happy path
- Validation errors
- Not-found / edge cases
- Security (service user writes, CSRF on forms)
- Build gate (mvn test -pl core)

Match the style in acceptance-criteria.md. Reference servlet paths under /bin/stms/ticket/* where applicable.
```

---

## Prompt 6 — Estimate file touch list

```text
For STMS feature "[feature name]", list every file likely to change (workspace-relative paths).

Group by module. Mark each as create | modify | optional.
Example vertical slice for ticket fields:
core TicketModel, TicketRepositoryImpl, servlet, ui.apps HTL/dialog, ui.config only if repoinit/index needed.

Minimal diff only — no unrelated refactors.
```

---

## Planning checklist (before implementation)

- [ ] Requirement mapped to FR/NFR or new IDs documented
- [ ] Existing code searched (no duplicate components/services)
- [ ] Modules identified (`core` + `ui.apps` minimum for ticket UI)
- [ ] Write path uses `TicketRepository` + service user (not request resolver)
- [ ] Acceptance criteria written and reviewable
- [ ] Phases have exit criteria (`mvn test -pl core`, local smoke URL)
- [ ] Out of scope explicitly stated

---

## Related prompts

- **Design:** `ai-prompts/design.md`
- **Implementation:** `ai-prompts/implementation.md`
- **Documentation:** `ai-prompts/documentation.md`
