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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.ttn.stms.core.testcontext.AppAemContext;
import com.ttn.stms.core.tickets.services.TicketRepository;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class TicketDetailModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private TicketRepository ticketRepository;
    private MockSlingHttpServletRequest request;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        context.registerService(TicketRepository.class, ticketRepository);

        context.create().page("/content/stms/us/en/ticket-detail");
        context.create().resource("/content/stms/us/en/ticket-detail/jcr:content/root/ticketdetail",
            "sling:resourceType", TicketDetailModel.RESOURCE_TYPE,
            "ticketId", "TICKET-DEFAULT",
            "listPage", "/content/stms/us/en/tickets",
            "editPage", "/content/stms/us/en/tickets/edit-ticket");

        context.currentResource("/content/stms/us/en/ticket-detail/jcr:content/root/ticketdetail");
        request = context.request();
    }

    @Test
    void testLoadsTicketFromRequestParameter() {
        TicketModel ticket = mock(TicketModel.class);
        when(ticket.getTicketId()).thenReturn("TICKET-0001");
        when(ticketRepository.getTicket(context.resourceResolver(), "TICKET-0001"))
            .thenReturn(Optional.of(ticket));

        request.setParameterMap(Map.of(TicketDetailModel.PARAM_TICKET_ID, "TICKET-0001"));

        TicketDetailModel model = request.adaptTo(TicketDetailModel.class);

        assertTrue(model.isFound());
        assertEquals("TICKET-0001", model.getTicketId());
        assertEquals("TICKET-0001", model.getTicket().getTicketId());
    }

    @Test
    void testFallsBackToDialogDefaultTicketId() {
        TicketModel ticket = mock(TicketModel.class);
        when(ticketRepository.getTicket(context.resourceResolver(), "TICKET-DEFAULT"))
            .thenReturn(Optional.of(ticket));

        TicketDetailModel model = request.adaptTo(TicketDetailModel.class);

        assertTrue(model.isFound());
        assertEquals("TICKET-DEFAULT", model.getTicketId());
    }

    @Test
    void testNotFoundWhenTicketMissing() {
        when(ticketRepository.getTicket(context.resourceResolver(), "TICKET-MISSING"))
            .thenReturn(Optional.empty());

        request.setParameterMap(Map.of(TicketDetailModel.PARAM_TICKET_ID, "TICKET-MISSING"));

        TicketDetailModel model = request.adaptTo(TicketDetailModel.class);

        assertTrue(model.isNotFound());
        assertEquals("TICKET-MISSING", model.getTicketId());
    }

    @Test
    void testListPageConfigured() {
        when(ticketRepository.getTicket(eq(context.resourceResolver()), eq("TICKET-DEFAULT")))
            .thenReturn(Optional.of(mock(TicketModel.class)));

        TicketDetailModel model = request.adaptTo(TicketDetailModel.class);

        assertTrue(model.hasListPage());
        assertEquals("/content/stms/us/en/tickets", model.getListPage());
    }

    @Test
    void testEditUrlConfigured() {
        when(ticketRepository.getTicket(context.resourceResolver(), "TICKET-0001"))
            .thenReturn(Optional.of(mock(TicketModel.class)));
        request.setParameterMap(Map.of(TicketDetailModel.PARAM_TICKET_ID, "TICKET-0001"));

        TicketDetailModel model = request.adaptTo(TicketDetailModel.class);

        assertTrue(model.hasEditUrl());
        assertEquals("/content/stms/us/en/tickets/edit-ticket.html?ticketId=TICKET-0001", model.getEditUrl());
    }
}
