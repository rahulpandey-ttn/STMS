# Prompt History — Documentation

---

## P-DOC-001 | AI workflow foundation

**Date:** 2026-09-01  
**Mode:** Agent

### Prompt

```text
Create content for AI Workflow Foundation (tool-workflow.md) from this project.
Cover: primary AI tool, project context, requirement analysis, planning, code generation,
validation, testing, debugging, code review, data boundaries, reuse.
```

### AI response

Large `tool-workflow.md` with AI lifecycle sections + STMS technical reference.

### Accepted

- Coverage checklist mapping to sections
- STMS module routing and ticket domain tables

### Changed

- Moved to repo root as `tool-workflow.md`
- Later restructured assessment docs separately per template

### Rejected

- Duplicating entire content in `AGENTS.md` — link instead

---

## P-DOC-002 | Assessment artifact set

**Date:** 2026-09-01  
**Mode:** Agent

### Prompt

```text
Generate candidate-info.md, requirements-analysis.md, acceptance-criteria.md,
implementation-plan.md, design-notes.md, api-contract.md, data-model.md,
ui-flow.md, test-strategy.md at project root.
```

### AI response

Nine markdown files aligned to implemented code.

### Accepted

- File set as assessment baseline
- Technical accuracy vs codebase

### Changed

- Second pass: restructured all files per official assessment template
- Added `code-review-notes.md`, `reflection.md`, `pr-description.md`

### Rejected

- Submitting without personal fields in `candidate-info.md` — filled before final submit

---

## P-DOC-003 | AI prompt playbooks

**Date:** 2026-09-01  
**Mode:** Agent

### Prompt

```text
Generate planning.md, design.md, implementation.md, testing.md, debugging.md,
code-review.md, documentation.md under ai-prompts folder.
```

### AI response

Seven uniform prompt template files.

### Accepted

- Reusable prompt patterns per lifecycle phase

### Changed

- Separated **templates** (`ai-prompts/*.md`) from **history** (`ai-prompts/history/`)

### Rejected

- Treating templates as prompt history — evaluator needs actual session log

---

## P-DOC-004 | Assessment template alignment

**Date:** 2026-09-01  
**Mode:** Agent

### Prompt

```text
Update all documents per assessment sample structure: candidate-info, requirements,
acceptance criteria, implementation plan, design notes, api contract, test strategy,
debugging notes, code review, reflection, PR description, prompt history by activity.
```

### AI response

Restructured all root docs; created `ai-prompts/history/*` with accept/reject entries.

### Accepted

- Template section headings from assessment rubric
- Checkbox format for acceptance criteria

### Changed

- Kept `data-model.md` and `ui-flow.md` as supplementary linked from design-notes

### Rejected

- Deleting technical depth entirely — linked supplementary docs instead
