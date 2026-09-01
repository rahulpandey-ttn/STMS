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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.ttn.stms.core.testcontext.AppAemContext;
import com.ttn.stms.core.tickets.services.TicketRepository;
import com.ttn.stms.core.tickets.services.TicketSearchCriteria;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class TicketListModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private TicketRepository ticketRepository;
    private MockSlingHttpServletRequest request;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        context.registerService(TicketRepository.class, ticketRepository);

        context.create().page("/content/stms/us/en/tickets");
        context.create().resource("/content/stms/us/en/tickets/jcr:content/root/ticketlist",
            "sling:resourceType", TicketListModel.RESOURCE_TYPE);

        context.currentResource("/content/stms/us/en/tickets/jcr:content/root/ticketlist");
        request = context.request();
    }

    @Test
    void testLoadsTicketsWithRequestFilters() {
        TicketModel ticket = mock(TicketModel.class);
        when(ticket.getTicketId()).thenReturn("TICKET-0001");

        request.setParameterMap(Map.of(
            TicketListModel.PARAM_STATUS, "open",
            TicketListModel.PARAM_ASSIGNEE, "rahul.pandey@ttn.com",
            TicketListModel.PARAM_PRIORITY, "high",
            TicketListModel.PARAM_CREATOR, "admin",
            TicketListModel.PARAM_SORT, TicketListModel.SORT_ASC
        ));

        when(ticketRepository.findTickets(any(), any(TicketSearchCriteria.class)))
            .thenReturn(Collections.singletonList(ticket));

        TicketListModel model = request.adaptTo(TicketListModel.class);

        assertFalse(model.isEmpty());
        assertEquals(1, model.getTickets().size());
        assertEquals("open", model.getStatusFilter());
        assertEquals("rahul.pandey@ttn.com", model.getAssigneeFilter());
        assertEquals("high", model.getPriorityFilter());
        assertEquals("admin", model.getCreatorFilter());
        assertEquals(TicketListModel.SORT_ASC, model.getSortOrder());
        assertTrue(model.isSortAscending());
    }

    @Test
    void testDefaultsToNewestFirstWhenNoSortProvided() {
        when(ticketRepository.findTickets(any(), any(TicketSearchCriteria.class)))
            .thenReturn(Collections.emptyList());

        TicketListModel model = request.adaptTo(TicketListModel.class);

        assertTrue(model.isEmpty());
        assertEquals(TicketListModel.SORT_DESC, model.getSortOrder());
        assertFalse(model.isSortAscending());
    }

    @Test
    void testPassesCriteriaToRepository() {
        request.setParameterMap(Map.of(TicketListModel.PARAM_STATUS, "resolved"));

        when(ticketRepository.findTickets(any(), any(TicketSearchCriteria.class)))
            .thenAnswer(invocation -> {
                TicketSearchCriteria criteria = invocation.getArgument(1);
                assertEquals("resolved", criteria.getStatus());
                assertFalse(criteria.isSortAscending());
                return Collections.emptyList();
            });

        request.adaptTo(TicketListModel.class);

        verify(ticketRepository).findTickets(any(), any(TicketSearchCriteria.class));
    }

    @Test
    void testStatusAndPriorityOptionsExposed() {
        when(ticketRepository.findTickets(any(), any(TicketSearchCriteria.class)))
            .thenReturn(Collections.emptyList());

        TicketListModel model = request.adaptTo(TicketListModel.class);

        assertEquals(4, model.getStatusOptions().length);
        assertEquals(4, model.getPriorityOptions().length);
    }

    @Test
    void testCreatePageConfigured() {
        ModifiableValueMap properties = context.resourceResolver()
            .getResource("/content/stms/us/en/tickets/jcr:content/root/ticketlist")
            .adaptTo(ModifiableValueMap.class);
        properties.put("detailPage", "/content/stms/us/en/ticket-detail");
        properties.put("createPage", "/content/stms/us/en/tickets/create-ticket");

        when(ticketRepository.findTickets(any(), any(TicketSearchCriteria.class)))
            .thenReturn(Collections.emptyList());

        TicketListModel model = request.adaptTo(TicketListModel.class);

        assertTrue(model.hasCreatePage());
        assertEquals("/content/stms/us/en/tickets/create-ticket", model.getCreatePage());
        assertTrue(model.hasDetailPage());
    }

    @Test
    void testStatusStats() {
        TicketModel openTicket = mock(TicketModel.class);
        when(openTicket.getStatusEnum()).thenReturn(com.ttn.stms.core.tickets.enums.TicketStatus.OPEN);

        TicketModel progressTicket = mock(TicketModel.class);
        when(progressTicket.getStatusEnum()).thenReturn(com.ttn.stms.core.tickets.enums.TicketStatus.IN_PROGRESS);

        TicketModel resolvedTicket = mock(TicketModel.class);
        when(resolvedTicket.getStatusEnum()).thenReturn(com.ttn.stms.core.tickets.enums.TicketStatus.RESOLVED);

        when(ticketRepository.findAllTickets(any())).thenReturn(
            java.util.List.of(openTicket, progressTicket, resolvedTicket));
        when(ticketRepository.findTickets(any(), any(TicketSearchCriteria.class)))
            .thenReturn(Collections.emptyList());

        TicketListModel model = request.adaptTo(TicketListModel.class);

        assertEquals(3, model.getTotalCount());
        assertEquals(1, model.getOpenCount());
        assertEquals(1, model.getInProgressCount());
        assertEquals(1, model.getResolvedCount());
    }
}
