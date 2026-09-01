/*
 *  Copyright 2026 TTN
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ttn.stms.core.tickets.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.ttn.stms.core.testcontext.AppAemContext;
import com.ttn.stms.core.tickets.enums.TicketPriority;
import com.ttn.stms.core.tickets.enums.TicketStatus;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class TicketModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private TicketModel ticket;

    @BeforeEach
    void setUp() {
        Calendar createdDate = new GregorianCalendar(2026, Calendar.AUGUST, 31, 10, 15, 0);
        createdDate.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

        Calendar commentDate1 = new GregorianCalendar(2026, Calendar.AUGUST, 31, 14, 30, 0);
        commentDate1.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

        Calendar commentDate2 = new GregorianCalendar(2026, Calendar.AUGUST, 31, 10, 15, 30);
        commentDate2.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

        context.create().resource("/content/stms/tickets/TICKET-0001",
            "jcr:primaryType", "nt:unstructured",
            "sling:resourceType", TicketModel.RESOURCE_TYPE,
            "ticketId", "TICKET-0001",
            "title", "Login page returns 500",
            "description", "Users cannot sign in after deploy.",
            "status", "open",
            "priority", "high",
            "assignee", "rahul.pandey@ttn.com",
            "createdDate", createdDate);

        context.create().resource("/content/stms/tickets/TICKET-0001/comments",
            "jcr:primaryType", "nt:unstructured",
            "sling:resourceType", TicketCommentsContainerModel.RESOURCE_TYPE);

        context.create().resource("/content/stms/tickets/TICKET-0001/comments/comment-late",
            "jcr:primaryType", "nt:unstructured",
            "sling:resourceType", TicketCommentModel.RESOURCE_TYPE,
            "commentId", "comment-late",
            "author", "rahul.pandey@ttn.com",
            "text", "Assigned to backend team.",
            "createdDate", commentDate1);

        context.create().resource("/content/stms/tickets/TICKET-0001/comments/comment-early",
            "jcr:primaryType", "nt:unstructured",
            "sling:resourceType", TicketCommentModel.RESOURCE_TYPE,
            "commentId", "comment-early",
            "author", "admin",
            "text", "Reproduced on Chrome 128.",
            "createdDate", commentDate2);

        Resource resource = context.resourceResolver().getResource("/content/stms/tickets/TICKET-0001");
        ticket = resource.adaptTo(TicketModel.class);
    }

    @Test
    void testTicketProperties() {
        assertNotNull(ticket);
        assertEquals("TICKET-0001", ticket.getTicketId());
        assertEquals("Login page returns 500", ticket.getTitle());
        assertEquals("Users cannot sign in after deploy.", ticket.getDescription());
        assertEquals("open", ticket.getStatus());
        assertEquals(TicketStatus.OPEN, ticket.getStatusEnum());
        assertEquals("high", ticket.getPriority());
        assertEquals(TicketPriority.HIGH, ticket.getPriorityEnum());
        assertEquals("rahul.pandey@ttn.com", ticket.getAssignee());
        assertNotNull(ticket.getCreatedDate());
    }

    @Test
    void testCommentsSortedByCreatedDate() {
        assertEquals(2, ticket.getComments().size());
        assertEquals("comment-early", ticket.getComments().get(0).getCommentId());
        assertEquals("comment-late", ticket.getComments().get(1).getCommentId());
        assertEquals("admin", ticket.getComments().get(0).getAuthor());
    }

    @Test
    void testInvalidEnumValuesReturnNull() {
        context.create().resource("/content/stms/tickets/TICKET-invalid",
            "jcr:primaryType", "nt:unstructured",
            "sling:resourceType", TicketModel.RESOURCE_TYPE,
            "status", "unknown",
            "priority", "urgent");

        Resource resource = context.resourceResolver().getResource("/content/stms/tickets/TICKET-invalid");
        TicketModel invalidTicket = resource.adaptTo(TicketModel.class);

        assertNotNull(invalidTicket);
        assertNull(invalidTicket.getStatusEnum());
        assertNull(invalidTicket.getPriorityEnum());
    }

    @Test
    void testEmptyCommentsWhenContainerMissing() {
        context.create().resource("/content/stms/tickets/TICKET-nocomments",
            "jcr:primaryType", "nt:unstructured",
            "sling:resourceType", TicketModel.RESOURCE_TYPE,
            "ticketId", "TICKET-nocomments");

        Resource resource = context.resourceResolver().getResource("/content/stms/tickets/TICKET-nocomments");
        TicketModel noCommentsTicket = resource.adaptTo(TicketModel.class);

        assertNotNull(noCommentsTicket);
        assertTrue(noCommentsTicket.getComments().isEmpty());
    }

    @Test
    void testPresentationHelpers() {
        assertEquals("open", ticket.getStatusBadgeClass());
        assertEquals("high", ticket.getPriorityLevelClass());
        assertEquals("RP", ticket.getAssigneeInitials());
        assertTrue(ticket.getAssigneeAvatarVariant() >= 1);
        assertTrue(ticket.getAssigneeAvatarVariant() <= 4);
        assertTrue(ticket.hasAssignee());
    }
}
