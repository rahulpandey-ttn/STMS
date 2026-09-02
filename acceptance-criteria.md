# Acceptance Criteria

Checklist for STMS baseline delivery. Map to tests in `test-strategy.md` and manual smoke on local author.

---

## Core

- [ ] Ticket list page loads at `/content/stms/us/en/tickets.html` with app shell visible
- [ ] All tickets under ticket repository `/content/stms/tickets` appear in list
- [ ] List supports sorting based on `creation-date`.
- [ ] List supports filters: `status`, `assignee`, `priority`, `creator`
- [ ] Detail page displays ticket fields for valid `ticketId`
- [ ] Comments render in chronological order on detail page.
- [ ] Create form submits and creates node `TICKET-NNNN` with `status=open`.
- [ ] Edit form updates title, description, status, priority, assignee
- [ ] Comment form adds child node under `comments/`
- [ ] App shell navigation links to list and create pages
- [ ] Six components registered: `appshell`, `ticketlist`, `ticketcreate`, `ticketdetail`, `ticketedit`, `ticketcomments`

---

## Validation

- [ ] Title is a required field.
- [ ] Length of title is max 200 chars
- [ ] Description is required.
- [ ] Comment is required.
- [ ] Comment Length is max 5000 chars

---

## Error Handling

- [ ] Validation errors redirect to form with `error` query param and preserved field values
- [ ] Errro message is ticket repository is not configured -- "Tickets folder is not configured."
- [ ] Error if service user not configured or missing -- "Ticket service is not available."
- [ ] GET on `/bin/stms/ticket/create|update|comment` returns 405

---

## Testing

- [ ] `mvn test -pl core` passes all test classes
- [ ] `mvn clean install` passes with AEM analyser
- [ ] Manual smoke: create → list → detail → edit → comment flow works


