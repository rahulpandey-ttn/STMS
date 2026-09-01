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
import com.ttn.stms.core.tickets.models.TicketCommentsModel;
import com.ttn.stms.core.tickets.models.TicketDetailModel;
import com.ttn.stms.core.tickets.services.TicketCommentCreateResult;
import com.ttn.stms.core.tickets.services.TicketRepository;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class TicketCommentServletTest {

    private final AemContext context = AppAemContext.newAemContext();

    private TicketCommentServlet servlet;
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        servlet = new TicketCommentServlet();

        context.registerService(TicketRepository.class, ticketRepository);
        context.registerInjectActivateService(servlet);
    }

    @Test
    void testRedirectsToDetailPageOnSuccess() throws Exception {
        when(ticketRepository.addComment(any())).thenReturn(TicketCommentCreateResult.success("comment-20260901-000000-001"));

        MockSlingHttpServletRequest request = context.request();
        request.setServletPath(TicketCommentServlet.SERVLET_PATH);
        request.setPathInfo(TicketCommentServlet.SERVLET_PATH);
        request.setMethod("POST");
        request.setParameterMap(java.util.Map.of(
            TicketCommentsModel.PARAM_TICKET_ID, "TICKET-0001",
            TicketCommentsModel.PARAM_TEXT, "Looks good to me",
            TicketCommentsModel.PARAM_DETAIL_PAGE, "/content/stms/us/en/ticket-detail"
        ));

        MockSlingHttpServletResponse response = context.response();
        servlet.doPost(request, response);

        assertEquals(302, response.getStatus());
        assertTrue(response.getHeader("Location").contains("/content/stms/us/en/ticket-detail.html?ticketId=TICKET-0001"));
        assertTrue(response.getHeader("Location").contains(TicketCommentsModel.PARAM_COMMENT_ADDED + "=true"));
    }
}
