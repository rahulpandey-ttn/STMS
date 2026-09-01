# AI Prompts — Documentation

Reusable prompts for **generating and updating STMS project documentation**.

**Tool:** Cursor Agent or Ask mode  
**Target docs:** Root `*.md`, `ai-prompts/`, `AGENTS.md` (read-only unless bootstrap)

---

## When to use

- After feature delivery (sync docs with code)
- Creating assessment / portfolio artifacts
- Updating API or data model docs
- Generating MR descriptions or release notes

---

## Context to attach

| Artifact | Why |
|---|---|
| Changed source files | Accurate API/schema docs |
| `git diff` | Scope for release notes |
| Existing doc | `api-contract.md`, `data-model.md`, etc. |
| `acceptance-criteria.md` | Traceability |

---

## Prompt 1 — Sync api-contract.md

```text
Update api-contract.md for STMS to reflect current servlet implementation.

Review:
- TicketCreateServlet, TicketEditServlet, TicketCommentServlet
- Parameter constants in TicketCreateModel, TicketEditModel, TicketCommentsModel
- Validation messages in TicketRepositoryImpl

Preserve document structure. Add new endpoints only if code exists.
```

---

## Prompt 2 — Sync data-model.md

```text
Update data-model.md for STMS from current code:

- TicketModel, TicketCommentModel, TicketCommentsContainerModel
- TicketStatus, TicketPriority enums
- TicketRepository interface and DTOs
- ui.config repoinit and stms-ticket-index

Include ASCII JCR tree if schema changed.
```

---

## Prompt 3 — Update acceptance criteria

```text
Add acceptance criteria for new STMS feature "[name]" to acceptance-criteria.md.

Format: Given / When / Then tables
IDs: continue AC-[section].[number] sequence
Link to test-strategy.md traceability where possible.
```

---

## Prompt 4 — Implementation plan update

```text
Update implementation-plan.md for STMS:

Mark phase "[phase name]" as Complete or In Progress.
Add Phase 6+ item for "[future feature]" with tasks and exit criteria.
Keep module dependency graph accurate.
```

---

## Prompt 5 — UI flow documentation

```text
Update ui-flow.md for STMS feature "[name]".

Add:
- New page path under /content/stms/us/en/...
- ASCII user flow
- Navigation matrix rows
- Query params and flash messages

Match existing ticket flow sections.
```

---

## Prompt 6 — Test strategy update

```text
Update test-strategy.md for STMS:

New tests added: [list test classes]
Map to acceptance criteria AC-x.x
Add E2E scenario if Cypress spec created.
Update traceability matrix table.
```

---

## Prompt 7 — MR / release description

```text
Write a GitLab merge request description for STMS changes:

Branch diff summary:
[paste git log --oneline or file list]

Include:
## Summary (bullets)
## Test plan (checkboxes with commands and URLs)
## Docs updated (list)
## Acceptance criteria addressed (AC-x.x)

Use conventional commit tone. Reference #issue if provided.
```

---

## Prompt 8 — AI workflow doc update

```text
Update ai-prompts/[planning|design|implementation|testing|debugging|code-review|documentation].md

Add a new prompt template for STMS capability: "[description]"
Follow existing section structure: When to use, Context, Prompt, Checklist.
Include STMS-specific paths and conventions.
```

---

## Prompt 9 — Feature README snippet

```text
Write a concise README section for STMS feature "[name]" suitable for project wiki:

- What it does (2 sentences)
- Author URLs (localhost:4502 paths)
- Servlet endpoints if any
- Key files (5–10 paths)
- How to test (one command + one manual step)

No marketing language. Technical audience.
```

---

## Prompt 10 — Generate full doc set from codebase

```text
From current STMS codebase, verify these root docs are accurate and list gaps only:

- requirements-analysis.md
- acceptance-criteria.md
- implementation-plan.md
- design-notes.md
- api-contract.md
- data-model.md
- ui-flow.md
- test-strategy.md

Output: table of File | Status (OK / Needs update) | Specific gap
Do not rewrite files unless gap is listed and I confirm.
```

---

## Documentation map

| Document | Update when |
|---|---|
| `api-contract.md` | Servlet params, paths, validation messages change |
| `data-model.md` | JCR properties, resource types, repository API change |
| `acceptance-criteria.md` | New user-facing behavior |
| `ui-flow.md` | New pages or navigation |
| `test-strategy.md` | New test types or CI gates |
| `design-notes.md` | New architecture decision (DD-N) |
| `implementation-plan.md` | Phase completion or new phase |
| `requirements-analysis.md` | New FR/NFR or scope change |
| `ai-prompts/*.md` | New repeatable AI workflows |
| `AGENTS.md` | Module or build structure changes (rare) |

---

## Documentation rules

- Docs reflect **implemented** behavior, not aspirations
- Use workspace-relative paths and real enum values (`open`, `in-progress`, …)
- Link between docs (api-contract ↔ data-model ↔ ui-flow)
- Do not duplicate `AGENTS.md` module catalog — link instead
- Keep `.res.local/` and secrets out of committed docs

---

## Documentation checklist

- [ ] Code and docs aligned (contract, model, criteria)
- [ ] New servlet documented in `api-contract.md`
- [ ] New properties in `data-model.md`
- [ ] MR description includes test plan
- [ ] ai-prompts updated if new workflow pattern established

---

## Related prompts

- **Planning:** `ai-prompts/planning.md`
- **Code review:** `ai-prompts/code-review.md` (pre-doc sync review)
