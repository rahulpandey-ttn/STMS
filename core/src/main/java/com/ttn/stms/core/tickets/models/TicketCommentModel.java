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

import java.util.Calendar;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

/**
 * Sling Model for a single ticket comment subnode.
 */
@Model(
    adaptables = Resource.class,
    resourceType = TicketCommentModel.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TicketCommentModel {

    public static final String RESOURCE_TYPE = "stms/tickets/comment";

    @ValueMapValue
    private String commentId;

    @ValueMapValue
    private String author;

    @ValueMapValue
    private String text;

    @ValueMapValue
    private Calendar createdDate;

    public String getCommentId() {
        return commentId;
    }

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public Calendar getCreatedDate() {
        return createdDate;
    }
}
