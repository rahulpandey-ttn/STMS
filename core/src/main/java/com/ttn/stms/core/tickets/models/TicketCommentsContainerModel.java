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

import javax.annotation.PostConstruct;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;

/**
 * Adapts the {@code comments} container node and exposes its child comment models.
 */
@Model(
    adaptables = Resource.class,
    resourceType = TicketCommentsContainerModel.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TicketCommentsContainerModel {

    public static final String RESOURCE_TYPE = "stms/tickets/comments";

    @ChildResource
    private List<TicketCommentModel> comments;

    private List<TicketCommentModel> sortedComments;

    @PostConstruct
    protected void init() {
        if (comments == null || comments.isEmpty()) {
            sortedComments = Collections.emptyList();
            return;
        }
        sortedComments = new ArrayList<>(comments);
        sortedComments.sort(Comparator.comparing(
            TicketCommentModel::getCreatedDate,
            Comparator.nullsLast(Comparator.naturalOrder())
        ));
    }

    public List<TicketCommentModel> getComments() {
        return sortedComments;
    }
}
