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
package com.ttn.stms.core.tickets.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.ttn.stms.core.testcontext.AppAemContext;
import com.ttn.stms.core.tickets.enums.TicketPriority;
import com.ttn.stms.core.tickets.enums.TicketStatus;
import com.ttn.stms.core.tickets.models.TicketModel;
import com.ttn.stms.core.tickets.services.TicketCreateRequest;
import com.ttn.stms.core.tickets.services.TicketCreateResult;
import com.ttn.stms.core.tickets.services.TicketRepository;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class TicketRepositoryImplCreateTest {

    private final AemContext context = AppAemContext.newAemContext();

    private TicketRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new TicketRepositoryImpl();
        context.create().resource(TicketRepository.TICKETS_ROOT,
            "jcr:primaryType", "sling:Folder",
            "sling:resourceType", "stms/tickets/folder");
        context.create().resource(TicketRepository.TICKETS_ROOT + "/TICKET-0003",
            "sling:resourceType", TicketModel.RESOURCE_TYPE,
            "ticketId", "TICKET-0003");
    }

    @Test
    void testGenerateNextTicketId() {
        Resource ticketsRoot = context.resourceResolver().getResource(TicketRepository.TICKETS_ROOT);
        assertEquals("TICKET-0004", repository.generateNextTicketId(ticketsRoot));
    }

    @Test
    void testCreateTicketSuccess() {
        TicketCreateRequest request = new TicketCreateRequest();
        request.setTitle("New issue");
        request.setDescription("Something is broken");
        request.setPriority(TicketPriority.HIGH.getValue());
        request.setAssignee("user@example.com");

        TicketCreateResult result = repository.createTicket(context.resourceResolver(), request);

        assertTrue(result.isSuccess());
        assertEquals("TICKET-0004", result.getTicketId());

        TicketModel ticket = context.resourceResolver()
            .getResource(TicketRepository.TICKETS_ROOT + "/TICKET-0004")
            .adaptTo(TicketModel.class);

        assertNotNull(ticket);
        assertEquals("New issue", ticket.getTitle());
        assertEquals(TicketStatus.OPEN, ticket.getStatusEnum());
        assertEquals(TicketPriority.HIGH, ticket.getPriorityEnum());
        assertEquals("user@example.com", ticket.getAssignee());
        assertNotNull(ticket.getCreatedDate());
        Resource ticketResource = context.resourceResolver().getResource(
            TicketRepository.TICKETS_ROOT + "/TICKET-0004");
        assertTrue(primaryTypeName(ticketResource).endsWith("unstructured"));
        assertEquals(TicketModel.RESOURCE_TYPE, ticketResource.getResourceType());

        Resource commentsResource = context.resourceResolver().getResource(
            TicketRepository.TICKETS_ROOT + "/TICKET-0004/comments");
        assertNotNull(commentsResource);
        assertTrue(primaryTypeName(commentsResource).endsWith("unstructured"));
    }

    private static String primaryTypeName(Resource resource) {
        String primaryType = resource.getValueMap().get("jcr:primaryType", String.class);
        if (primaryType != null && primaryType.contains("}")) {
            return primaryType.substring(primaryType.indexOf('}') + 1);
        }
        return primaryType;
    }

    @Test
    void testCreateTicketValidationFailure() {
        TicketCreateRequest request = new TicketCreateRequest();
        request.setTitle("");
        request.setDescription("Missing title");
        request.setPriority(TicketPriority.LOW.getValue());

        TicketCreateResult result = repository.createTicket(context.resourceResolver(), request);

        assertFalse(result.isSuccess());
        assertEquals("Title is required.", result.getErrorMessage());
    }
}
