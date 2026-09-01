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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.ttn.stms.core.testcontext.AppAemContext;
import com.ttn.stms.core.tickets.enums.TicketPriority;
import com.ttn.stms.core.tickets.enums.TicketStatus;
import com.ttn.stms.core.tickets.models.TicketModel;
import com.ttn.stms.core.tickets.services.TicketRepository;
import com.ttn.stms.core.tickets.services.TicketUpdateRequest;
import com.ttn.stms.core.tickets.services.TicketUpdateResult;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class TicketRepositoryImplUpdateTest {

    private final AemContext context = AppAemContext.newAemContext();

    private TicketRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new TicketRepositoryImpl();
        context.create().resource(TicketRepository.TICKETS_ROOT,
            "jcr:primaryType", "sling:Folder",
            "sling:resourceType", "stms/tickets/folder");
        context.create().resource(TicketRepository.TICKETS_ROOT + "/TICKET-0001",
            "sling:resourceType", TicketModel.RESOURCE_TYPE,
            "ticketId", "TICKET-0001",
            "title", "Original title",
            "description", "Original description",
            "status", TicketStatus.OPEN.getValue(),
            "priority", TicketPriority.LOW.getValue(),
            "assignee", "old@example.com");
    }

    @Test
    void testUpdateTicketSuccess() {
        TicketUpdateRequest request = new TicketUpdateRequest();
        request.setTicketId("TICKET-0001");
        request.setTitle("Updated title");
        request.setDescription("Updated description");
        request.setStatus(TicketStatus.IN_PROGRESS.getValue());
        request.setPriority(TicketPriority.HIGH.getValue());
        request.setAssignee("new@example.com");

        TicketUpdateResult result = repository.updateTicket(context.resourceResolver(), request);

        assertTrue(result.isSuccess());
        assertEquals("TICKET-0001", result.getTicketId());

        TicketModel ticket = context.resourceResolver()
            .getResource(TicketRepository.TICKETS_ROOT + "/TICKET-0001")
            .adaptTo(TicketModel.class);

        assertEquals("Updated title", ticket.getTitle());
        assertEquals("Updated description", ticket.getDescription());
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatusEnum());
        assertEquals(TicketPriority.HIGH, ticket.getPriorityEnum());
        assertEquals("new@example.com", ticket.getAssignee());
    }

    @Test
    void testUpdateTicketNotFound() {
        TicketUpdateRequest request = new TicketUpdateRequest();
        request.setTicketId("TICKET-MISSING");
        request.setTitle("Title");
        request.setDescription("Description");
        request.setStatus(TicketStatus.OPEN.getValue());
        request.setPriority(TicketPriority.MEDIUM.getValue());

        TicketUpdateResult result = repository.updateTicket(context.resourceResolver(), request);

        assertFalse(result.isSuccess());
        assertEquals("Ticket was not found.", result.getErrorMessage());
    }

    @Test
    void testUpdateTicketValidationFailure() {
        TicketUpdateRequest request = new TicketUpdateRequest();
        request.setTicketId("TICKET-0001");
        request.setTitle("");
        request.setDescription("Description");
        request.setStatus(TicketStatus.OPEN.getValue());
        request.setPriority(TicketPriority.MEDIUM.getValue());

        TicketUpdateResult result = repository.updateTicket(context.resourceResolver(), request);

        assertFalse(result.isSuccess());
        assertEquals("Title is required.", result.getErrorMessage());
    }
}
