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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.ttn.stms.core.tickets.enums.TicketPriority;
import com.ttn.stms.core.tickets.enums.TicketStatus;

/**
 * Sling Model for a single support ticket node under {@code /content/stms/tickets}.
 */
@Model(
    adaptables = Resource.class,
    resourceType = TicketModel.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TicketModel {

    public static final String RESOURCE_TYPE = "stms/tickets/ticket";

    @ValueMapValue
    private String ticketId;

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String description;

    @ValueMapValue
    private String status;

    @ValueMapValue
    private String priority;

    @ValueMapValue
    private String assignee;

    @ValueMapValue
    private Calendar createdDate;

    @ValueMapValue(name = "jcr:createdBy")
    private String createdBy;

    @SlingObject
    private Resource currentResource;

    private List<TicketCommentModel> comments;

    @PostConstruct
    protected void init() {
        Resource commentsResource = currentResource.getChild("comments");
        if (commentsResource == null) {
            comments = Collections.emptyList();
            return;
        }
        comments = StreamSupport.stream(commentsResource.getChildren().spliterator(), false)
            .map(child -> child.adaptTo(TicketCommentModel.class))
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
        comments.sort(Comparator.comparing(
            TicketCommentModel::getCreatedDate,
            Comparator.nullsLast(Comparator.naturalOrder())
        ));
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public TicketStatus getStatusEnum() {
        return TicketStatus.fromValue(status);
    }

    public String getPriority() {
        return priority;
    }

    public TicketPriority getPriorityEnum() {
        return TicketPriority.fromValue(priority);
    }

    public String getAssignee() {
        return assignee;
    }

    public Calendar getCreatedDate() {
        return createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public List<TicketCommentModel> getComments() {
        return comments != null ? comments : Collections.emptyList();
    }

    /**
     * CSS modifier for status badges ({@code open}, {@code progress}, {@code resolved}, {@code closed}).
     */
    public String getStatusBadgeClass() {
        TicketStatus statusEnum = getStatusEnum();
        if (statusEnum == TicketStatus.IN_PROGRESS) {
            return "progress";
        }
        if (statusEnum == TicketStatus.RESOLVED) {
            return "resolved";
        }
        if (statusEnum == TicketStatus.CLOSED) {
            return "closed";
        }
        return "open";
    }

    /**
     * CSS modifier for priority indicators ({@code high}, {@code medium}, {@code low}).
     */
    public String getPriorityLevelClass() {
        TicketPriority priorityEnum = getPriorityEnum();
        if (priorityEnum == TicketPriority.HIGH || priorityEnum == TicketPriority.CRITICAL) {
            return "high";
        }
        if (priorityEnum == TicketPriority.MEDIUM) {
            return "medium";
        }
        return "low";
    }

    /**
     * Two-letter initials derived from the assignee email for avatar display.
     */
    public String getAssigneeInitials() {
        if (StringUtils.isBlank(assignee)) {
            return "";
        }
        String localPart = assignee.split("@")[0];
        String[] parts = localPart.split("[.\\-_]");
        if (parts.length >= 2 && StringUtils.isNotBlank(parts[0]) && StringUtils.isNotBlank(parts[1])) {
            return ("" + Character.toUpperCase(parts[0].charAt(0))
                + Character.toUpperCase(parts[1].charAt(0)));
        }
        if (localPart.length() >= 2) {
            return localPart.substring(0, 2).toUpperCase();
        }
        return localPart.toUpperCase();
    }

    /**
     * Avatar colour variant (1–4) derived from assignee email.
     */
    public int getAssigneeAvatarVariant() {
        if (StringUtils.isBlank(assignee)) {
            return 1;
        }
        return (Math.abs(assignee.hashCode()) % 4) + 1;
    }

    public boolean hasAssignee() {
        return StringUtils.isNotBlank(assignee);
    }
}
