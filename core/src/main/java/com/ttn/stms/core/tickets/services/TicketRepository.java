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
package com.ttn.stms.core.tickets.services;

import java.util.List;
import java.util.Optional;

import org.apache.sling.api.resource.ResourceResolver;

import com.ttn.stms.core.tickets.models.TicketModel;

/**
 * OSGi service for reading support ticket nodes from the repository.
 */
public interface TicketRepository {

    String TICKETS_ROOT = "/content/stms/tickets";

    /**
     * Loads a single ticket by its ID (node name).
     */
    Optional<TicketModel> getTicket(ResourceResolver resourceResolver, String ticketId);

    /**
     * Finds tickets with optional status and assignee filters.
     *
     * @param limit maximum results; use {@code -1} for no limit
     */
    List<TicketModel> findTickets(ResourceResolver resourceResolver, String status, String assignee, int limit);

    /**
     * Finds tickets using the supplied search, filter, and sort criteria.
     */
    List<TicketModel> findTickets(ResourceResolver resourceResolver, TicketSearchCriteria criteria);

    /**
     * Returns all tickets ordered by creation date descending.
     */
    List<TicketModel> findAllTickets(ResourceResolver resourceResolver);

    /**
     * Creates a new ticket under {@link #TICKETS_ROOT} using the ticket-write service user.
     */
    TicketCreateResult createTicket(TicketCreateRequest request);

    /**
     * Adds a comment to an existing ticket using the ticket-write service user.
     */
    TicketCommentCreateResult addComment(TicketCommentCreateRequest request);

    /**
     * Updates an existing ticket using the ticket-write service user.
     */
    TicketUpdateResult updateTicket(TicketUpdateRequest request);
}
