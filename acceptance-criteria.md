# Acceptance Criteria

Checklist for STMS baseline delivery. Map to tests in `test-strategy.md` and manual smoke on local author.

---

## Core

- [ ] Ticket list page loads at `/content/stms/us/en/tickets.html` with app shell visible
- [ ] All tickets under `/content/stms/tickets` with `sling:resourceType=stms/tickets/ticket` appear in list
- [ ] List supports sort `createdDate-asc` and `createdDate-desc`
- [ ] List supports filters: `status`, `assignee`, `priority`, `creator`
- [ ] Detail page displays ticket fields for valid `?ticketId=TICKET-NNNN`
- [ ] Comments render in chronological order on detail page
- [ ] Create form submits and creates node `TICKET-NNNN` with `status=open`
- [ ] Edit form updates title, description, status, priority, assignee
- [ ] Comment form adds child node under `comments/`
- [ ] App shell navigation links to list and create pages
- [ ] Six components registered: `appshell`, `ticketlist`, `ticketcreate`, `ticketdetail`, `ticketedit`, `ticketcomments`

---

## Validation

- [ ] Create rejects blank title: "Title is required."
- [ ] Create rejects title > 200 chars
- [ ] Create rejects blank description
- [ ] Create rejects invalid priority
- [ ] Update rejects invalid status or priority
- [ ] Comment rejects blank text
- [ ] Comment rejects text > 5000 chars
- [ ] Client-side validation on create form before POST

---

## Error Handling

- [ ] Validation errors redirect to form with `error` query param and preserved field values
- [ ] Missing tickets folder returns "Tickets folder is not configured."
- [ ] Service user failure returns "Ticket service is not available."
- [ ] Unknown ticket on update returns "Ticket was not found."
- [ ] GET on `/bin/stms/ticket/create|update|comment` returns 405
- [ ] Missing `ticketId` on detail does not throw 500

---

## Testing

- [ ] `mvn test -pl core` passes (18+ test classes)
- [ ] `mvn clean install` passes with AEM analyser
- [ ] `TicketCreateIT` compiles; passes against local author when packages deployed
- [ ] Manual smoke: create → list → detail → edit → comment flow works
- [ ] `dispatcher/bin/validate.sh src` passes (if dispatcher touched)

---

## Documentation

- [ ] `candidate-info.md` complete (no placeholders)
- [ ] `requirements-analysis.md`, `design-notes.md`, `api-contract.md` aligned with code
- [ ] `ai-prompts/history/` contains reviewed prompt entries with accept/reject notes
- [ ] `debugging-notes.md` documents at least 3 real issues
- [ ] `reflection.md` and `code-review-notes.md` present
- [ ] `pr-description.md` summarizes deliverable for review
