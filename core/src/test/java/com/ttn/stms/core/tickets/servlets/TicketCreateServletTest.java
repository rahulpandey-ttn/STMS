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
package com.ttn.stms.core.tickets.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.ttn.stms.core.testcontext.AppAemContext;
import com.ttn.stms.core.tickets.models.TicketCreateModel;
import com.ttn.stms.core.tickets.services.TicketCreateResult;
import com.ttn.stms.core.tickets.services.TicketRepository;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class TicketCreateServletTest {

    private final AemContext context = AppAemContext.newAemContext();

    private TicketCreateServlet servlet;
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        servlet = new TicketCreateServlet();

        context.registerService(TicketRepository.class, ticketRepository);
        context.registerInjectActivateService(servlet);
    }

    @Test
    void testRedirectsToDetailPageOnSuccess() throws Exception {
        when(ticketRepository.createTicket(any())).thenReturn(TicketCreateResult.success("TICKET-0004"));

        MockSlingHttpServletRequest request = context.request();
        request.setServletPath(TicketCreateServlet.SERVLET_PATH);
        request.setPathInfo(TicketCreateServlet.SERVLET_PATH);
        request.setMethod("POST");
        request.setParameterMap(java.util.Map.of(
            TicketCreateModel.PARAM_TITLE, "Broken login",
            TicketCreateModel.PARAM_DESCRIPTION, "Cannot sign in",
            TicketCreateModel.PARAM_PRIORITY, "high",
            TicketCreateModel.PARAM_DETAIL_PAGE, "/content/stms/us/en/ticket-detail",
            TicketCreateModel.PARAM_FORM_PAGE, "/content/stms/us/en/create-ticket"
        ));

        MockSlingHttpServletResponse response = context.response();
        servlet.doPost(request, response);

        assertEquals(302, response.getStatus());
        assertTrue(response.getHeader("Location").contains("/content/stms/us/en/ticket-detail.html?ticketId=TICKET-0004"));
    }
}
