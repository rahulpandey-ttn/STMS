# STMS Feature & Design Prompts

---

## Operations & diagnostics

1. Review the local AEM author log tail and summarize any active OSGi warnings.

---

## Foundation & architecture

2. Design a concrete JCR node schema under `/content/stms/tickets` for a Support Ticket Management System on AEM Cloud SDK. Each ticket node should store: id, title, description, status (Open, In Progress, Resolved, Closed), priority, assignee, creation date, and comments (as child nodes or a structured array). Provide a clear ASCII directory structure for the node design and an OSGi-friendly Sling Model architecture outline for reading these nodes dynamically.

3. Implement the Support Ticket JCR Schema and Sling Model Architecture plan as specified. Do not edit the plan file. Complete all existing to-dos in order, marking each as in progress while working through them.

---

## Ticket components

4. Confirm whether a component exists to list all tickets.

5. Create a ticket listing component that displays all tickets, supports sorting by creation time, and allows filtering by creator, status, and related fields.

6. Build a component to display ticket details.

7. Build a component to create a new ticket.

8. Add client-side validation to the create-ticket form and add a “Create ticket” action on the ticket list component.

9. Create a component for adding comments to a ticket.

10. Add the ability to edit an existing ticket.

---

## UI & layout

11. Apply the visual design from `stms-tickets-redesign.html` component by component, and place shared styling in the base client library.

12. Add a sidebar and top bar to the application shell.
