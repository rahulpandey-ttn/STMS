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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import javax.jcr.Session;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.ttn.stms.core.testcontext.AppAemContext;
import com.ttn.stms.core.tickets.models.TicketModel;
import com.ttn.stms.core.tickets.services.TicketRepository;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class TicketRepositoryImplTest {

    private final AemContext context = AppAemContext.newAemContext();

    private TicketRepositoryImpl repository;

    @BeforeEach
    void setUp() throws Exception {
        repository = new TicketRepositoryImpl();
        QueryBuilder queryBuilder = mock(QueryBuilder.class);
        setField(repository, "queryBuilder", queryBuilder);
        context.registerService(QueryBuilder.class, queryBuilder);
        context.registerAdapter(org.apache.sling.api.resource.ResourceResolver.class, Session.class, mock(Session.class));
    }

    @Test
    void testGetTicketFound() {
        createTicketResource("TICKET-0001", "open", "rahul.pandey@ttn.com");

        assertTrue(repository.getTicket(context.resourceResolver(), "TICKET-0001").isPresent());
        TicketModel ticket = repository.getTicket(context.resourceResolver(), "TICKET-0001").get();
        assertEquals("TICKET-0001", ticket.getTicketId());
        assertEquals("open", ticket.getStatus());
    }

    @Test
    void testGetTicketNotFound() {
        assertFalse(repository.getTicket(context.resourceResolver(), "TICKET-missing").isPresent());
        assertFalse(repository.getTicket(context.resourceResolver(), "").isPresent());
        assertFalse(repository.getTicket(null, "TICKET-0001").isPresent());
    }

    @Test
    void testFindTicketsViaQueryBuilder() throws Exception {
        Resource openTicket = createTicketResource("TICKET-open", "open", "rahul.pandey@ttn.com");
        createTicketResource("TICKET-closed", "closed", "rahul.pandey@ttn.com");

        QueryBuilder queryBuilder = context.getService(QueryBuilder.class);
        Query query = mock(Query.class);
        SearchResult searchResult = mock(SearchResult.class);
        Hit hit = mock(Hit.class);

        when(queryBuilder.createQuery(any(PredicateGroup.class), any())).thenReturn(query);
        when(query.getResult()).thenReturn(searchResult);
        when(searchResult.getHits()).thenReturn(Collections.singletonList(hit));
        when(hit.getResource()).thenReturn(openTicket);

        List<TicketModel> tickets = repository.findTickets(context.resourceResolver(), "open", null, 10);

        assertEquals(1, tickets.size());
        assertEquals("TICKET-open", tickets.get(0).getTicketId());
    }

    @Test
    void testFindAllTicketsDelegatesToFindTickets() throws Exception {
        Resource ticket = createTicketResource("TICKET-0003", "resolved", "author@ttn.com");

        QueryBuilder queryBuilder = context.getService(QueryBuilder.class);
        Query query = mock(Query.class);
        SearchResult searchResult = mock(SearchResult.class);
        Hit hit = mock(Hit.class);

        when(queryBuilder.createQuery(any(PredicateGroup.class), any())).thenReturn(query);
        when(query.getResult()).thenReturn(searchResult);
        when(searchResult.getHits()).thenReturn(Collections.singletonList(hit));
        when(hit.getResource()).thenReturn(ticket);

        List<TicketModel> tickets = repository.findAllTickets(context.resourceResolver());

        assertEquals(1, tickets.size());
        assertEquals(TicketRepository.TICKETS_ROOT + "/TICKET-0003", ticket.getPath());
    }

    private Resource createTicketResource(String ticketId, String status, String assignee) {
        return context.create().resource(TicketRepository.TICKETS_ROOT + "/" + ticketId,
            "jcr:primaryType", "nt:unstructured",
            "sling:resourceType", TicketModel.RESOURCE_TYPE,
            "ticketId", ticketId,
            "title", "Test ticket",
            "status", status,
            "assignee", assignee);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
