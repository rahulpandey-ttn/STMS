# Code Review Notes

## AI-Assisted Review Summary

I used Cursor Ask mode and Agent mode to review STMS changes before consolidating documentation. Review focused on Cloud Service compliance, module boundaries, and ticket write security.

**Prompt used:**

```text
Review my STMS changes for merge readiness.
Check: module placement, service-user writes, no /libs writes, OSGi DS R6,
HTL vs Model separation, CSRF on forms, unit tests updated, no secrets, minimal scope.
Format: Critical / Suggestion / Nice to have.
```

**AI findings (summary):**

| Severity | Finding |
|---|---|
| Critical | None after `ui.config` deploy fix |
| Suggestion | Ensure all HTL forms include `:cq_csrf_token` |
| Suggestion | Extend unit tests for validation edge cases (comment length) |
| Nice to have | Add `_cq_design_dialog` for template policy authoring |
| Nice to have | Integration test for update and comment servlets |

---

## My Review Observations

### Strengths

- **Repository pattern** is consistent — servlets do not touch JCR directly
- **Service user** correctly used for all writes (`stms-ticket-write`)
- **Module separation** respected: Java in `core`, HTL in `ui.apps`, config in `ui.config`
- **Sling Models** use `@OSGiService` for `TicketRepository` injection
- **Unit tests** cover happy path and key validation failures for repository and servlets
- **No `/libs` modifications**; repoinit in `ui.config`

### Issues I found manually

1. **Deploy order matters** — `core` alone insufficient; need `ui.config` + `ui.content` (see `debugging-notes.md`)
2. **QueryBuilder** excludes nodes without `sling:resourceType=stms/tickets/ticket`
3. **`candidate-info.md`** had placeholder fields — fixed before submission
4. **Documentation** initially written retrospectively — addressed with `ai-prompts/history/` and this review pass

### Security check

- [x] No hardcoded credentials in source
- [x] No `loginAdministrative`
- [x] Input length limits enforced server-side
- [x] POST-only write servlets

---

## Changes Made After Review

| Change | Reason |
|---|---|
| Added `TicketCreateIT` | Integration test evidence for create servlet |
| Restructured docs per assessment template | Clearer evaluator navigation |
| Created `ai-prompts/history/` with accept/reject per prompt | Authenticity of AI workflow |
| Expanded `debugging-notes.md` to Issue template | Required artifact format |
| Fixed `TicketRepositoryImplAddCommentTest` assertion | Align with validation message |
| Filled `candidate-info.md` personal fields | Ownership / completeness |

---

## Suggestions Rejected (and why)

| AI suggestion | Why rejected |
|---|---|
| Replace form POST with JSON REST API | Out of MVP scope; HTL forms are the intended AEM pattern |
| Use Content Fragments for tickets | JCR nodes already implemented; CF adds complexity without MVP benefit |
| Add React SPA for ticket UI | Conflicts with HTL/AEM authoring approach; unnecessary dependency |
| Auto-commit from Agent without review | Assessment requires human review of diffs |
| Store assignee as `/home/users/...` path only | Product assumption: email string is sufficient for MVP |
| Broad refactor of archetype boilerplate (`HelloWorld`, etc.) | Out of scope; increases review noise |

---

## Follow-up review items (Phase 6)

- [ ] Add `TicketEditIT` and `TicketCommentIT`
- [ ] Cypress spec for create → detail flow
- [ ] `_cq_design_dialog` for ticket components if template policies required
- [ ] `code-assessment` scan for `unbounded-query` on `TicketRepositoryImpl`

**Related:** `ai-prompts/history/code-review.md`, `acceptance-criteria.md`
