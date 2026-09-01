# AI Prompts — Code Review

Reusable prompts for **reviewing STMS changes** before commit or merge request.

**Tool:** Cursor Ask mode or Agent (read-only review)  
**Skills:** `.agents/skills/code-assessment/` (optional automated scan)

---

## When to use

- Before opening a GitLab MR
- After Agent-generated implementation
- Periodic Cloud Service compliance check
- Reviewing ticket feature PRs

---

## Context to attach

| Artifact | Why |
|---|---|
| `git diff` or changed files | Scope of review |
| `design-notes.md` | Expected architecture |
| `api-contract.md` | Servlet contract compliance |
| `acceptance-criteria.md` | Feature completeness |
| `.cursorrules` | Project conventions |

---

## Prompt 1 — Full diff review (STMS)

```text
Review my STMS changes for merge readiness.

Check:
1. Correct module placement (core vs ui.apps vs ui.config vs ui.content)
2. Ticket writes use TicketRepository + stms-ticket-write (no admin session)
3. No /libs modifications
4. OSGi DS R6 annotations
5. HTL free of business logic; Sling Models used correctly
6. Forms include CSRF token for POST servlets
7. Unit tests updated in core/src/test
8. No secrets, prod URLs, or credentials
9. Minimal scope — no unrelated refactors

Format findings as:
- Critical (must fix)
- Suggestion (should consider)
- Nice to have

Reference file paths and line numbers where possible.
```

---

## Prompt 2 — Security review

```text
Security review for STMS diff:

[paste diff summary or @files]

Focus:
- Service user usage for JCR writes
- Input validation (title 200, comment 5000, enums)
- XSS in HTL (context attributes, @ context='html')
- Servlet POST-only for mutations
- No hardcoded passwords (pom local admin is dev-only)
- ACL changes in repoinit — principle of least privilege

List issues by severity with remediation.
```

---

## Prompt 3 — Cloud Service compliance

```text
Review STMS changes for AEM as a Cloud Service compliance.

Use code-assessment patterns where relevant:
- inject-in-sling-model (@OSGiService in models)
- unbounded-query (TicketRepositoryImpl limits)
- deprecated APIs
- resource-change-listener / scheduler best practices if touched

Confirm mvn install would pass aemanalyser. List violations only.
```

---

## Prompt 4 — API contract compliance

```text
Verify servlet changes match api-contract.md for STMS:

Endpoints: /bin/stms/ticket/create | update | comment

Check parameter names, validation messages, redirect URLs, GET→405 behavior.
Report any breaking changes to api-contract.md that need doc update.
```

---

## Prompt 5 — Test coverage review

```text
Review test coverage for STMS feature "[name]":

Changed production files: [list]
Existing tests: [list]

Identify untested paths (validation branches, error redirects, edge cases).
Recommend specific test methods to add — class names only, match existing test style.
```

---

## Prompt 6 — Component / HTL review

```text
Review AEM component [name] in ui.apps for STMS:

- Dialog: Coral 3 Granite structure
- HTL: valid data-sly-* , correct Model injection
- clientlibs: proper categories, no duplicate site CSS
- Accessibility: labels, error alerts
- Consistency with ticket* components

No code changes — review comments only.
```

---

## Prompt 7 — Pre-MR checklist generation

```text
Generate a merge request checklist for STMS branch "[branch]" based on changed files.

Include:
- Test commands run
- Manual smoke URLs
- Docs to update (api-contract, data-model, acceptance-criteria)
- Dispatcher validate needed? (yes/no)
- Risk summary (1 paragraph)

Conventional commit title suggestion: feat|fix|chore(stms): ...
```

---

## STMS review rubric

| Area | Pass criteria |
|---|---|
| **Architecture** | Repository pattern; servlets thin |
| **Security** | Service user writes; validated inputs |
| **Modules** | Java in core; HTL in ui.apps |
| **Tests** | `mvn test -pl core` green; new behavior tested |
| **Docs** | Contract/model docs updated if API/schema changed |
| **Scope** | Single feature; no drive-by cleanup |
| **CS** | No deprecated APIs; no `/libs` writes |

---

## Automated review (optional)

```text
Run code-assessment skill in report mode for pattern: [inject-in-sling-model | unbounded-query | outdated-dependencies]

Do not apply fixes — report findings only for MR description.
```

---

## Code review checklist (human + AI)

- [ ] Critical issues resolved
- [ ] `mvn test -pl core` passes
- [ ] Manual smoke on author (if UI/servlet change)
- [ ] No secrets in diff
- [ ] MR description references acceptance criteria IDs
- [ ] api-contract.md / data-model.md updated if needed

---

## Related prompts

- **Testing:** `ai-prompts/testing.md`
- **Documentation:** `ai-prompts/documentation.md`
