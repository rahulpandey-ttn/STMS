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

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.ttn.stms.core.tickets.enums.TicketPriority;
import com.ttn.stms.core.tickets.enums.TicketStatus;
import com.ttn.stms.core.tickets.services.TicketRepository;
import com.ttn.stms.core.tickets.services.TicketSearchCriteria;

/**
 * Sling Model for the ticket list component. Supports request-parameter filters and sorting.
 */
@Model(
    adaptables = SlingHttpServletRequest.class,
    resourceType = TicketListModel.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TicketListModel {

    public static final String RESOURCE_TYPE = "stms/components/ticketlist";

    public static final String PARAM_STATUS = "status";
    public static final String PARAM_ASSIGNEE = "assignee";
    public static final String PARAM_PRIORITY = "priority";
    public static final String PARAM_CREATOR = "creator";
    public static final String PARAM_SORT = "sort";

    public static final String SORT_ASC = "createdDate-asc";
    public static final String SORT_DESC = "createdDate-desc";

    private static final int DEFAULT_LIMIT = -1;

    @OSGiService
    private TicketRepository ticketRepository;

    @SlingObject
    private ResourceResolver resourceResolver;

    @SlingObject
    private SlingHttpServletRequest request;

    @ValueMapValue
    @Default(intValues = DEFAULT_LIMIT)
    private int limit;

    @ValueMapValue
    private String detailPage;

    @ValueMapValue
    private String createPage;

    private List<TicketModel> tickets;
    private String statusFilter;
    private String assigneeFilter;
    private String priorityFilter;
    private String creatorFilter;
    private String sortOrder;
    private int totalCount;
    private int openCount;
    private int inProgressCount;
    private int resolvedCount;

    @PostConstruct
    protected void init() {
        statusFilter = request.getParameter(PARAM_STATUS);
        assigneeFilter = request.getParameter(PARAM_ASSIGNEE);
        priorityFilter = request.getParameter(PARAM_PRIORITY);
        creatorFilter = request.getParameter(PARAM_CREATOR);
        sortOrder = StringUtils.defaultIfBlank(request.getParameter(PARAM_SORT), SORT_DESC);

        if (ticketRepository == null || resourceResolver == null) {
            tickets = Collections.emptyList();
            return;
        }

        loadStats();

        TicketSearchCriteria criteria = new TicketSearchCriteria();
        criteria.setStatus(statusFilter);
        criteria.setAssignee(assigneeFilter);
        criteria.setPriority(priorityFilter);
        criteria.setCreator(creatorFilter);
        criteria.setSortAscending(SORT_ASC.equals(sortOrder));
        criteria.setLimit(limit);

        tickets = ticketRepository.findTickets(resourceResolver, criteria);
    }

    private void loadStats() {
        for (TicketModel ticket : ticketRepository.findAllTickets(resourceResolver)) {
            totalCount++;
            TicketStatus status = ticket.getStatusEnum();
            if (status == TicketStatus.OPEN) {
                openCount++;
            } else if (status == TicketStatus.IN_PROGRESS) {
                inProgressCount++;
            } else if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
                resolvedCount++;
            }
        }
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getOpenCount() {
        return openCount;
    }

    public int getInProgressCount() {
        return inProgressCount;
    }

    public int getResolvedCount() {
        return resolvedCount;
    }

    public List<TicketModel> getTickets() {
        return tickets;
    }

    public boolean isEmpty() {
        return tickets == null || tickets.isEmpty();
    }

    public String getStatusFilter() {
        return StringUtils.defaultString(statusFilter);
    }

    public String getAssigneeFilter() {
        return StringUtils.defaultString(assigneeFilter);
    }

    public String getPriorityFilter() {
        return StringUtils.defaultString(priorityFilter);
    }

    public String getCreatorFilter() {
        return StringUtils.defaultString(creatorFilter);
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public boolean isSortAscending() {
        return SORT_ASC.equals(sortOrder);
    }

    public TicketStatus[] getStatusOptions() {
        return TicketStatus.values();
    }

    public TicketPriority[] getPriorityOptions() {
        return TicketPriority.values();
    }

    public String getDetailPage() {
        return detailPage;
    }

    public boolean hasDetailPage() {
        return StringUtils.isNotBlank(detailPage);
    }

    public String getCreatePage() {
        return createPage;
    }

    public boolean hasCreatePage() {
        return StringUtils.isNotBlank(createPage);
    }

    public String getTicketDetailUrl(String ticketId) {
        if (StringUtils.isBlank(detailPage) || StringUtils.isBlank(ticketId)) {
            return null;
        }
        return detailPage + ".html?" + PARAM_TICKET_ID + "=" + ticketId;
    }

    public static final String PARAM_TICKET_ID = "ticketId";
}
