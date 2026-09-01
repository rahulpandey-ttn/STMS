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

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.ttn.stms.core.tickets.enums.TicketPriority;
import com.ttn.stms.core.tickets.enums.TicketStatus;
import com.ttn.stms.core.tickets.services.TicketRepository;

/**
 * Sling Model for the edit-ticket form component.
 */
@Model(
    adaptables = SlingHttpServletRequest.class,
    resourceType = TicketEditModel.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TicketEditModel {

    public static final String RESOURCE_TYPE = "stms/components/ticketedit";

    public static final String PARAM_TICKET_ID = TicketDetailModel.PARAM_TICKET_ID;

    public static final String PARAM_ERROR = "error";

    public static final String PARAM_TITLE = "title";

    public static final String PARAM_DESCRIPTION = "description";

    public static final String PARAM_STATUS = "status";

    public static final String PARAM_PRIORITY = "priority";

    public static final String PARAM_ASSIGNEE = "assignee";

    public static final String PARAM_FORM_PAGE = "formPage";

    public static final String PARAM_DETAIL_PAGE = "detailPage";

    public static final String SERVLET_PATH = "/bin/stms/ticket/update";

    @OSGiService
    private TicketRepository ticketRepository;

    @SlingObject
    private ResourceResolver resourceResolver;

    @SlingObject
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String detailPage;

    @ValueMapValue
    private String listPage;

    @ValueMapValue
    private String ticketId;

    private String resolvedTicketId;
    private TicketModel ticket;
    private String errorMessage;
    private String titleValue;
    private String descriptionValue;
    private String statusValue;
    private String priorityValue;
    private String assigneeValue;
    private boolean repost;

    @PostConstruct
    protected void init() {
        resolvedTicketId = StringUtils.defaultIfBlank(request.getParameter(PARAM_TICKET_ID), ticketId);
        errorMessage = request.getParameter(PARAM_ERROR);
        repost = StringUtils.isNotBlank(errorMessage);

        if (ticketRepository == null || resourceResolver == null || StringUtils.isBlank(resolvedTicketId)) {
            return;
        }

        ticket = ticketRepository.getTicket(resourceResolver, resolvedTicketId).orElse(null);
        if (ticket == null) {
            return;
        }

        if (repost) {
            titleValue = StringUtils.defaultString(request.getParameter(PARAM_TITLE));
            descriptionValue = StringUtils.defaultString(request.getParameter(PARAM_DESCRIPTION));
            statusValue = StringUtils.defaultIfBlank(request.getParameter(PARAM_STATUS), TicketStatus.OPEN.getValue());
            priorityValue = StringUtils.defaultIfBlank(request.getParameter(PARAM_PRIORITY), TicketPriority.MEDIUM.getValue());
            assigneeValue = StringUtils.defaultString(request.getParameter(PARAM_ASSIGNEE));
        } else {
            titleValue = StringUtils.defaultString(ticket.getTitle());
            descriptionValue = StringUtils.defaultString(ticket.getDescription());
            statusValue = StringUtils.defaultIfBlank(ticket.getStatus(), TicketStatus.OPEN.getValue());
            priorityValue = StringUtils.defaultIfBlank(ticket.getPriority(), TicketPriority.MEDIUM.getValue());
            assigneeValue = StringUtils.defaultString(ticket.getAssignee());
        }
    }

    public TicketModel getTicket() {
        return ticket;
    }

    public boolean isFound() {
        return ticket != null;
    }

    public boolean isNotFound() {
        return !isFound();
    }

    public boolean hasTicketId() {
        return StringUtils.isNotBlank(resolvedTicketId);
    }

    public String getTicketId() {
        return StringUtils.defaultString(resolvedTicketId);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasError() {
        return StringUtils.isNotBlank(errorMessage);
    }

    public String getTitleValue() {
        return titleValue;
    }

    public String getDescriptionValue() {
        return descriptionValue;
    }

    public String getStatusValue() {
        return statusValue;
    }

    public String getPriorityValue() {
        return priorityValue;
    }

    public String getAssigneeValue() {
        return assigneeValue;
    }

    public String getDetailPage() {
        return detailPage;
    }

    public boolean hasDetailPage() {
        return StringUtils.isNotBlank(detailPage);
    }

    public String getListPage() {
        return listPage;
    }

    public boolean hasListPage() {
        return StringUtils.isNotBlank(listPage);
    }

    public TicketStatus[] getStatusOptions() {
        return TicketStatus.values();
    }

    public TicketPriority[] getPriorityOptions() {
        return TicketPriority.values();
    }

    public String getFormAction() {
        return SERVLET_PATH;
    }
}
