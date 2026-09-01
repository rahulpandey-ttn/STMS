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
package com.ttn.stms.core.shell.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.ttn.stms.core.testcontext.AppAemContext;
import com.ttn.stms.core.tickets.models.TicketListModel;
import com.ttn.stms.core.tickets.models.TicketModel;
import com.ttn.stms.core.tickets.services.TicketRepository;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class AppShellModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private TicketRepository ticketRepository;
    private MockSlingHttpServletRequest request;

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(AppShellModel.class);

        ticketRepository = mock(TicketRepository.class);
        context.registerService(TicketRepository.class, ticketRepository);

        context.create().page("/content/stms/us/en/tickets");
        context.create().resource("/content/stms/us/en/tickets/jcr:content/root",
            "sling:resourceType", AppShellModel.RESOURCE_TYPE,
            "ticketsListPage", "/content/stms/us/en/tickets",
            "createTicketPage", "/content/stms/us/en/tickets/create-ticket");

        context.currentPage("/content/stms/us/en/tickets");
        context.currentResource("/content/stms/us/en/tickets/jcr:content/root");
        request = context.request();
    }

    @Test
    void testWorkspaceNavigationAndCounts() {
        TicketModel assignedTicket = mock(TicketModel.class);
        when(assignedTicket.getAssignee()).thenReturn("admin");
        when(assignedTicket.getCreatedBy()).thenReturn("author");

        when(ticketRepository.findAllTickets(any())).thenReturn(Collections.singletonList(assignedTicket));

        AppShellModel model = request.adaptTo(AppShellModel.class);
        assertNotNull(model, "AppShellModel should adapt from request");

        assertEquals(1, model.getTotalCount());
        assertEquals(4, model.getWorkspaceNav().size());
        assertEquals("All tickets", model.getWorkspaceNav().get(0).getLabel());
        assertTrue(model.getWorkspaceNav().get(0).isActive());
        assertEquals("/content/stms/us/en/tickets.html", model.getTicketsListUrl());
        assertEquals("/content/stms/us/en/tickets/create-ticket.html", model.getCreateTicketUrl());
    }

    @Test
    void testAssignedToMeActiveWhenFiltered() {
        when(ticketRepository.findAllTickets(any())).thenReturn(Collections.emptyList());

        request.setParameterMap(Map.of(TicketListModel.PARAM_ASSIGNEE, "anonymous"));
        context.currentPage("/content/stms/us/en/tickets");
        context.currentResource("/content/stms/us/en/tickets/jcr:content/root");
        request = context.request();

        AppShellModel model = request.adaptTo(AppShellModel.class);
        assertNotNull(model);

        assertFalse(model.getWorkspaceNav().get(0).isActive());
        assertTrue(model.getWorkspaceNav().get(1).isActive());
        assertTrue(model.getWorkspaceNav().get(1).getHref().contains("assignee=anonymous"));
    }
}
