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
import com.ttn.stms.core.tickets.models.TicketCommentModel;
import com.ttn.stms.core.tickets.models.TicketCommentsContainerModel;
import com.ttn.stms.core.tickets.models.TicketModel;
import com.ttn.stms.core.tickets.services.TicketCommentCreateRequest;
import com.ttn.stms.core.tickets.services.TicketCommentCreateResult;
import com.ttn.stms.core.tickets.services.TicketRepository;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class TicketRepositoryImplAddCommentTest {

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
            "ticketId", "TICKET-0001");
        context.create().resource(TicketRepository.TICKETS_ROOT + "/TICKET-0001/comments",
            "sling:resourceType", TicketCommentsContainerModel.RESOURCE_TYPE);
    }

    @Test
    void testAddCommentSuccess() {
        TicketCommentCreateRequest request = new TicketCommentCreateRequest();
        request.setTicketId("TICKET-0001");
        request.setAuthor("admin");
        request.setText("Please investigate this issue.");

        TicketCommentCreateResult result = repository.addComment(context.resourceResolver(), request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getCommentId());
        assertTrue(result.getCommentId().startsWith("comment-"));

        Resource commentResource = context.resourceResolver().getResource(
            TicketRepository.TICKETS_ROOT + "/TICKET-0001/comments/" + result.getCommentId());
        assertNotNull(commentResource);
        assertEquals(TicketCommentModel.RESOURCE_TYPE, commentResource.getResourceType());

        TicketCommentModel comment = commentResource.adaptTo(TicketCommentModel.class);
        assertNotNull(comment);
        assertEquals("admin", comment.getAuthor());
        assertEquals("Please investigate this issue.", comment.getText());
        assertNotNull(comment.getCreatedDate());
    }

    @Test
    void testAddCommentCreatesCommentsContainerWhenMissing() {
        context.create().resource(TicketRepository.TICKETS_ROOT + "/TICKET-0002",
            "sling:resourceType", TicketModel.RESOURCE_TYPE,
            "ticketId", "TICKET-0002");

        TicketCommentCreateRequest request = new TicketCommentCreateRequest();
        request.setTicketId("TICKET-0002");
        request.setAuthor("author");
        request.setText("First comment");

        TicketCommentCreateResult result = repository.addComment(context.resourceResolver(), request);

        assertTrue(result.isSuccess());
        Resource commentsResource = context.resourceResolver().getResource(
            TicketRepository.TICKETS_ROOT + "/TICKET-0002/comments");
        assertNotNull(commentsResource);
        assertEquals(TicketCommentsContainerModel.RESOURCE_TYPE, commentsResource.getResourceType());
    }

    @Test
    void testAddCommentValidationFailure() {
        TicketCommentCreateRequest request = new TicketCommentCreateRequest();
        request.setTicketId("TICKET-0001");
        request.setAuthor("admin");
        request.setText("");

        TicketCommentCreateResult result = repository.addComment(context.resourceResolver(), request);

        assertFalse(result.isSuccess());
        assertEquals("Comment text is required.", result.getErrorMessage());
    }
}
