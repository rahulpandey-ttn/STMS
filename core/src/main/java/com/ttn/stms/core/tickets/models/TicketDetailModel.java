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

import com.ttn.stms.core.tickets.services.TicketRepository;

/**
 * Sling Model for the ticket detail component. Loads a ticket by request parameter or dialog default.
 */
@Model(
    adaptables = SlingHttpServletRequest.class,
    resourceType = TicketDetailModel.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TicketDetailModel {

    public static final String RESOURCE_TYPE = "stms/components/ticketdetail";

    public static final String PARAM_TICKET_ID = "ticketId";

    public static final String PARAM_CREATED = "created";

    public static final String PARAM_UPDATED = "updated";

    @OSGiService
    private TicketRepository ticketRepository;

    @SlingObject
    private ResourceResolver resourceResolver;

    @SlingObject
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String ticketId;

    @ValueMapValue
    private String listPage;

    @ValueMapValue
    private String editPage;

    private TicketModel ticket;
    private String resolvedTicketId;
    private boolean created;
    private boolean updated;

    @PostConstruct
    protected void init() {
        resolvedTicketId = StringUtils.defaultIfBlank(request.getParameter(PARAM_TICKET_ID), ticketId);
        created = "true".equalsIgnoreCase(request.getParameter(PARAM_CREATED));
        updated = "true".equalsIgnoreCase(request.getParameter(PARAM_UPDATED));

        if (ticketRepository == null || resourceResolver == null || StringUtils.isBlank(resolvedTicketId)) {
            return;
        }

        ticket = ticketRepository.getTicket(resourceResolver, resolvedTicketId).orElse(null);
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

    public String getTicketId() {
        return StringUtils.defaultString(resolvedTicketId);
    }

    public String getListPage() {
        return listPage;
    }

    public boolean hasListPage() {
        return StringUtils.isNotBlank(listPage);
    }

    public boolean isCreated() {
        return created;
    }

    public boolean isUpdated() {
        return updated;
    }

    public String getEditPage() {
        return editPage;
    }

    public boolean hasEditPage() {
        return StringUtils.isNotBlank(editPage);
    }

    public boolean hasEditUrl() {
        return isFound() && hasEditPage();
    }

    public String getEditUrl() {
        if (!hasEditUrl()) {
            return "";
        }
        return editPage + ".html?" + PARAM_TICKET_ID + "=" + resolvedTicketId;
    }
}
