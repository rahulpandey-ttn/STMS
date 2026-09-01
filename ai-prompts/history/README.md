# Prompt History — STMS

Prompt history grouped by **activity** for assessment review. Templates for future use live in `ai-prompts/planning.md`, etc. — this folder records **what actually happened**.

## How to read each entry

| Field | Meaning |
|---|---|
| **Prompt** | Text sent to Cursor (verbatim or close paraphrase) |
| **AI response** | What the agent proposed or produced |
| **Accepted** | What I kept as-is |
| **Changed** | What I modified before committing |
| **Rejected** | What I declined and why |

## Index

| Activity | File |
|---|---|
| Planning & requirements | [planning.md](./planning.md) |
| Design & architecture | [design.md](./design.md) |
| Implementation | [implementation.md](./implementation.md) |
| Testing | [testing.md](./testing.md) |
| Debugging | [debugging.md](./debugging.md) |
| Code review | [code-review.md](./code-review.md) |
| Documentation | [documentation.md](./documentation.md) |

## Also see

- `STMS-propmts-history.md` — original numbered prompt list
- `ticket_jcr_schema_design_24dcc4d5.plan.md` — Plan mode output (design)
- `prompt-log.md` — chronological index (links here)

## Strong signals this folder provides

- Context setting (`@TicketCreateServlet`, module scope)
- Iteration (service user fix after first deploy)
- Review of AI output (rejected JSON API, rejected SPA)
- Stack-specific guidance (repoinit, service user, QueryBuilder)
- Testing and validation prompts
- Responsible scope (no `/libs`, no secrets in prompts)
