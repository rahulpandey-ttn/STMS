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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.ttn.stms.core.tickets.enums.TicketPriority;
import com.ttn.stms.core.tickets.enums.TicketStatus;
import com.ttn.stms.core.tickets.models.TicketCommentModel;
import com.ttn.stms.core.tickets.models.TicketCommentsContainerModel;
import com.ttn.stms.core.tickets.models.TicketModel;
import com.ttn.stms.core.tickets.services.TicketCommentCreateRequest;
import com.ttn.stms.core.tickets.services.TicketCommentCreateResult;
import com.ttn.stms.core.tickets.services.TicketCreateRequest;
import com.ttn.stms.core.tickets.services.TicketCreateResult;
import com.ttn.stms.core.tickets.services.TicketRepository;
import com.ttn.stms.core.tickets.services.TicketSearchCriteria;
import com.ttn.stms.core.tickets.services.TicketUpdateRequest;
import com.ttn.stms.core.tickets.services.TicketUpdateResult;

/**
 * Default {@link TicketRepository} implementation using QueryBuilder for listings.
 */
@Component(service = TicketRepository.class)
public class TicketRepositoryImpl implements TicketRepository {

    private static final Logger LOG = LoggerFactory.getLogger(TicketRepositoryImpl.class);

    static final String TICKET_WRITE_SUBSERVICE = "stms-ticket-write";

    private static final String JCR_PRIMARY_TYPE = "jcr:primaryType";

    private static final String TICKET_ID_PREFIX = "TICKET-";

    private static final String COMMENT_ID_PREFIX = "comment-";

    private static final int COMMENT_TEXT_MAX_LENGTH = 5000;

    private static final int TITLE_MAX_LENGTH = 200;

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public Optional<TicketModel> getTicket(ResourceResolver resourceResolver, String ticketId) {
        if (resourceResolver == null || StringUtils.isBlank(ticketId)) {
            return Optional.empty();
        }
        Resource resource = resourceResolver.getResource(TICKETS_ROOT + "/" + ticketId);
        if (resource == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(resource.adaptTo(TicketModel.class));
    }

    @Override
    public List<TicketModel> findTickets(ResourceResolver resourceResolver, String status, String assignee, int limit) {
        TicketSearchCriteria criteria = new TicketSearchCriteria();
        criteria.setStatus(status);
        criteria.setAssignee(assignee);
        criteria.setLimit(limit);
        return findTickets(resourceResolver, criteria);
    }

    @Override
    public List<TicketModel> findTickets(ResourceResolver resourceResolver, TicketSearchCriteria criteria) {
        if (resourceResolver == null || criteria == null) {
            return Collections.emptyList();
        }
        Session session = resourceResolver.adaptTo(Session.class);
        if (session == null) {
            return Collections.emptyList();
        }

        Map<String, String> predicates = buildPredicates(criteria);
        Query query = queryBuilder.createQuery(PredicateGroup.create(predicates), session);
        SearchResult result = query.getResult();

        List<TicketModel> tickets = new ArrayList<>();
        for (Hit hit : result.getHits()) {
            try {
                Resource resource = hit.getResource();
                if (resource != null) {
                    TicketModel ticket = resource.adaptTo(TicketModel.class);
                    if (ticket != null) {
                        tickets.add(ticket);
                    }
                }
            } catch (Exception e) {
                // skip hits that cannot be resolved
            }
        }
        return tickets;
    }

    @Override
    public List<TicketModel> findAllTickets(ResourceResolver resourceResolver) {
        return findTickets(resourceResolver, null, null, -1);
    }

    @Override
    public TicketCreateResult createTicket(TicketCreateRequest request) {
        if (request == null) {
            return TicketCreateResult.failure("Ticket request is required.");
        }

        String validationError = validateCreateRequest(request);
        if (validationError != null) {
            return TicketCreateResult.failure(validationError);
        }

        try (ResourceResolver resolver = getTicketWriteResolver()) {
            return createTicket(resolver, request);
        } catch (LoginException e) {
            LOG.error("Unable to obtain ticket-write service resource resolver", e);
            return TicketCreateResult.failure("Ticket service is not available.");
        }
    }

    TicketCreateResult createTicket(ResourceResolver resolver, TicketCreateRequest request) {
        String validationError = validateCreateRequest(request);
        if (validationError != null) {
            return TicketCreateResult.failure(validationError);
        }

        Resource ticketsRoot = resolver.getResource(TICKETS_ROOT);
        if (ticketsRoot == null) {
            return TicketCreateResult.failure("Tickets folder is not configured.");
        }

        String ticketId = generateNextTicketId(ticketsRoot);
        try {
            Map<String, Object> ticketProperties = new HashMap<>();
            ticketProperties.put(JCR_PRIMARY_TYPE, NodeType.NT_UNSTRUCTURED);
            ticketProperties.put(ResourceResolver.PROPERTY_RESOURCE_TYPE, TicketModel.RESOURCE_TYPE);

            Resource ticketResource = resolver.create(ticketsRoot, ticketId, ticketProperties);

            ModifiableValueMap properties = ticketResource.adaptTo(ModifiableValueMap.class);
            if (properties == null) {
                return TicketCreateResult.failure("Unable to create ticket properties.");
            }

            Calendar createdDate = new GregorianCalendar();
            properties.put("ticketId", ticketId);
            properties.put("title", StringUtils.trim(request.getTitle()));
            properties.put("description", StringUtils.trim(request.getDescription()));
            properties.put("status", TicketStatus.OPEN.getValue());
            properties.put("priority", request.getPriority());
            properties.put("assignee", StringUtils.trimToNull(request.getAssignee()));
            properties.put("createdDate", createdDate);

            Map<String, Object> commentsProperties = new HashMap<>();
            commentsProperties.put(JCR_PRIMARY_TYPE, NodeType.NT_UNSTRUCTURED);
            commentsProperties.put(
                ResourceResolver.PROPERTY_RESOURCE_TYPE,
                TicketCommentsContainerModel.RESOURCE_TYPE
            );
            resolver.create(ticketResource, "comments", commentsProperties);

            resolver.commit();
            return TicketCreateResult.success(ticketId);
        } catch (PersistenceException e) {
            LOG.error("Failed to persist ticket {}", ticketId, e);
            return TicketCreateResult.failure("Unable to save the ticket. Please try again.");
        }
    }

    @Override
    public TicketCommentCreateResult addComment(TicketCommentCreateRequest request) {
        if (request == null) {
            return TicketCommentCreateResult.failure("Comment request is required.");
        }

        String validationError = validateCommentRequest(request);
        if (validationError != null) {
            return TicketCommentCreateResult.failure(validationError);
        }

        try (ResourceResolver resolver = getTicketWriteResolver()) {
            return addComment(resolver, request);
        } catch (LoginException e) {
            LOG.error("Unable to obtain ticket-write service resource resolver", e);
            return TicketCommentCreateResult.failure("Ticket service is not available.");
        }
    }

    TicketCommentCreateResult addComment(ResourceResolver resolver, TicketCommentCreateRequest request) {
        String validationError = validateCommentRequest(request);
        if (validationError != null) {
            return TicketCommentCreateResult.failure(validationError);
        }

        Resource ticketResource = resolver.getResource(TICKETS_ROOT + "/" + request.getTicketId());
        if (ticketResource == null) {
            return TicketCommentCreateResult.failure("Ticket was not found.");
        }

        try {
            Resource commentsResource = ticketResource.getChild("comments");
            if (commentsResource == null) {
                Map<String, Object> commentsProperties = new HashMap<>();
                commentsProperties.put(JCR_PRIMARY_TYPE, NodeType.NT_UNSTRUCTURED);
                commentsProperties.put(
                    ResourceResolver.PROPERTY_RESOURCE_TYPE,
                    TicketCommentsContainerModel.RESOURCE_TYPE
                );
                commentsResource = resolver.create(ticketResource, "comments", commentsProperties);
            }

            String commentId = generateCommentId(commentsResource);
            Map<String, Object> commentProperties = new HashMap<>();
            commentProperties.put(JCR_PRIMARY_TYPE, NodeType.NT_UNSTRUCTURED);
            commentProperties.put(ResourceResolver.PROPERTY_RESOURCE_TYPE, TicketCommentModel.RESOURCE_TYPE);

            Resource commentResource = resolver.create(commentsResource, commentId, commentProperties);
            ModifiableValueMap properties = commentResource.adaptTo(ModifiableValueMap.class);
            if (properties == null) {
                return TicketCommentCreateResult.failure("Unable to create comment properties.");
            }

            properties.put("commentId", commentId);
            properties.put("author", StringUtils.trim(request.getAuthor()));
            properties.put("text", StringUtils.trim(request.getText()));
            properties.put("createdDate", new GregorianCalendar());

            resolver.commit();
            return TicketCommentCreateResult.success(commentId);
        } catch (PersistenceException e) {
            LOG.error("Failed to persist comment for ticket {}", request.getTicketId(), e);
            return TicketCommentCreateResult.failure("Unable to save the comment. Please try again.");
        }
    }

    @Override
    public TicketUpdateResult updateTicket(TicketUpdateRequest request) {
        if (request == null) {
            return TicketUpdateResult.failure("Ticket update request is required.");
        }

        String validationError = validateUpdateRequest(request);
        if (validationError != null) {
            return TicketUpdateResult.failure(validationError);
        }

        try (ResourceResolver resolver = getTicketWriteResolver()) {
            return updateTicket(resolver, request);
        } catch (LoginException e) {
            LOG.error("Unable to obtain ticket-write service resource resolver", e);
            return TicketUpdateResult.failure("Ticket service is not available.");
        }
    }

    TicketUpdateResult updateTicket(ResourceResolver resolver, TicketUpdateRequest request) {
        String validationError = validateUpdateRequest(request);
        if (validationError != null) {
            return TicketUpdateResult.failure(validationError);
        }

        Resource ticketResource = resolver.getResource(TICKETS_ROOT + "/" + request.getTicketId());
        if (ticketResource == null) {
            return TicketUpdateResult.failure("Ticket was not found.");
        }

        try {
            ModifiableValueMap properties = ticketResource.adaptTo(ModifiableValueMap.class);
            if (properties == null) {
                return TicketUpdateResult.failure("Unable to update ticket properties.");
            }

            properties.put("title", StringUtils.trim(request.getTitle()));
            properties.put("description", StringUtils.trim(request.getDescription()));
            properties.put("status", request.getStatus());
            properties.put("priority", request.getPriority());
            properties.put("assignee", StringUtils.trimToNull(request.getAssignee()));

            resolver.commit();
            return TicketUpdateResult.success(request.getTicketId());
        } catch (PersistenceException e) {
            LOG.error("Failed to update ticket {}", request.getTicketId(), e);
            return TicketUpdateResult.failure("Unable to save the ticket. Please try again.");
        }
    }

    ResourceResolver getTicketWriteResolver() throws LoginException {
        Map<String, Object> authInfo = Collections.singletonMap(
            ResourceResolverFactory.SUBSERVICE,
            TICKET_WRITE_SUBSERVICE
        );
        return resourceResolverFactory.getServiceResourceResolver(authInfo);
    }

    private String validateCommentRequest(TicketCommentCreateRequest request) {
        if (StringUtils.isBlank(request.getTicketId())) {
            return "Ticket ID is required.";
        }
        if (StringUtils.isBlank(request.getAuthor())) {
            return "Author is required.";
        }
        if (StringUtils.isBlank(request.getText())) {
            return "Comment text is required.";
        }
        if (StringUtils.length(request.getText()) > COMMENT_TEXT_MAX_LENGTH) {
            return "Comment must be " + COMMENT_TEXT_MAX_LENGTH + " characters or fewer.";
        }
        return null;
    }

    String generateCommentId(Resource commentsResource) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
        formatter.setTimeZone(TimeZone.getDefault());
        String timestamp = formatter.format(new Date());
        String prefix = COMMENT_ID_PREFIX + timestamp + "-";

        int maxSequence = 0;
        for (Resource child : commentsResource.getChildren()) {
            if (ResourceUtil.isNonExistingResource(child)) {
                continue;
            }
            String name = child.getName();
            if (name.startsWith(prefix)) {
                try {
                    int sequence = Integer.parseInt(name.substring(prefix.length()));
                    maxSequence = Math.max(maxSequence, sequence);
                } catch (NumberFormatException ignored) {
                    // skip non-matching comment node names
                }
            }
        }

        return prefix + String.format("%03d", maxSequence + 1);
    }

    private String validateCreateRequest(TicketCreateRequest request) {
        if (StringUtils.isBlank(request.getTitle())) {
            return "Title is required.";
        }
        if (StringUtils.length(request.getTitle()) > TITLE_MAX_LENGTH) {
            return "Title must be " + TITLE_MAX_LENGTH + " characters or fewer.";
        }
        if (StringUtils.isBlank(request.getDescription())) {
            return "Description is required.";
        }
        if (TicketPriority.fromValue(request.getPriority()) == null) {
            return "A valid priority is required.";
        }
        return null;
    }

    private String validateUpdateRequest(TicketUpdateRequest request) {
        if (StringUtils.isBlank(request.getTicketId())) {
            return "Ticket ID is required.";
        }
        if (StringUtils.isBlank(request.getTitle())) {
            return "Title is required.";
        }
        if (StringUtils.length(request.getTitle()) > TITLE_MAX_LENGTH) {
            return "Title must be " + TITLE_MAX_LENGTH + " characters or fewer.";
        }
        if (StringUtils.isBlank(request.getDescription())) {
            return "Description is required.";
        }
        if (TicketStatus.fromValue(request.getStatus()) == null) {
            return "A valid status is required.";
        }
        if (TicketPriority.fromValue(request.getPriority()) == null) {
            return "A valid priority is required.";
        }
        return null;
    }

    String generateNextTicketId(Resource ticketsRoot) {
        int maxNumber = 0;
        for (Resource child : ticketsRoot.getChildren()) {
            if (ResourceUtil.isNonExistingResource(child)) {
                continue;
            }
            String name = child.getName();
            if (name.startsWith(TICKET_ID_PREFIX)) {
                try {
                    int number = Integer.parseInt(name.substring(TICKET_ID_PREFIX.length()));
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException ignored) {
                    // skip non-numeric ticket node names
                }
            }
        }
        return String.format("%s%04d", TICKET_ID_PREFIX, maxNumber + 1);
    }

    private Map<String, String> buildPredicates(TicketSearchCriteria criteria) {
        Map<String, String> predicates = new HashMap<>();
        predicates.put("path", TICKETS_ROOT);
        predicates.put("type", "nt:unstructured");
        predicates.put("property", "sling:resourceType");
        predicates.put("property.value", TicketModel.RESOURCE_TYPE);

        int propertyIndex = 1;
        propertyIndex = addPropertyPredicate(predicates, propertyIndex, "status", criteria.getStatus());
        propertyIndex = addPropertyPredicate(predicates, propertyIndex, "assignee", criteria.getAssignee());
        propertyIndex = addPropertyPredicate(predicates, propertyIndex, "priority", criteria.getPriority());
        addPropertyPredicate(predicates, propertyIndex, "jcr:createdBy", criteria.getCreator());

        predicates.put("orderby", "@createdDate");
        predicates.put("orderby.sort", criteria.isSortAscending() ? "asc" : "desc");

        if (criteria.getLimit() > 0) {
            predicates.put("p.limit", String.valueOf(criteria.getLimit()));
        }

        return predicates;
    }

    private int addPropertyPredicate(Map<String, String> predicates, int index, String name, String value) {
        if (StringUtils.isBlank(value)) {
            return index;
        }
        String prefix = index + "_property";
        predicates.put(prefix, name);
        predicates.put(prefix + ".value", value);
        return index + 1;
    }
}
