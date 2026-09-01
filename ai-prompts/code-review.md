# Prompt History — Code Review

---

## P-REV-001 | Pre-submission diff review

**Date:** 2026-09-01  
**Mode:** Ask

### Prompt

```text
Review my STMS changes for merge readiness.
Check: module placement, service-user writes, no /libs writes, OSGi DS R6,
HTL vs Model separation, CSRF on forms, unit tests updated, no secrets, minimal scope.
Format: Critical / Suggestion / Nice to have.
```

### AI response

No critical issues; suggestions for CSRF verification, design dialogs, more IT coverage.

### Accepted

- CSRF check on all forms
- Integration test expansion as follow-up

### Changed

- Documented findings in `code-review-notes.md`

### Rejected

- AI "nice to have" to refactor all archetype demo components — out of scope

---

## P-REV-002 | Cloud Service compliance scan

**Date:** 2026-09-01  
**Mode:** Ask

### Prompt

```text
Review TicketRepositoryImpl for unbounded QueryBuilder usage and Cloud Service issues.
```

### AI response

Noted `p.limit` from criteria; suggested verifying default limit behavior.

### Accepted

- Review of predicate structure

### Changed

- Confirmed `TicketSearchCriteria.limit` default `-1` is intentional for author scale

### Rejected

- Adding pagination UI in same sprint — deferred to Phase 6

---

## P-REV-003 | Documentation authenticity review

**Date:** 2026-09-01  
**Mode:** Ask (self-initiated after evaluation feedback)

### Prompt

```text
My assessment feedback says docs look AI-generated retrospectively.
What artifacts should I add to improve authenticity without adding more boilerplate?
```

### AI response

Suggested `debugging-notes.md`, prompt log with accept/reject, raw chat exports, `TicketCreateIT` run output.

### Accepted

- Restructure docs per assessment template
- Create `ai-prompts/history/` with per-prompt accept/reject

### Changed

- Shortened boilerplate; added first-person sections in `reflection.md`

### Rejected

- Generating more uniform template files — would worsen signal
