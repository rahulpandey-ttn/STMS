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
package com.ttn.stms.core.shell.models;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.ttn.stms.core.tickets.models.TicketListModel;
import com.ttn.stms.core.tickets.models.TicketModel;
import com.ttn.stms.core.tickets.services.TicketRepository;

/**
 * Sling Model for the STMS application shell (sidebar and top bar).
 */
@Model(
    adaptables = SlingHttpServletRequest.class,
    resourceType = AppShellModel.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class AppShellModel {

    public static final String RESOURCE_TYPE = "stms/components/appshell";

    @OSGiService
    private TicketRepository ticketRepository;

    @SlingObject
    private ResourceResolver resourceResolver;

    @SlingObject
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String ticketsListPage;

    @ValueMapValue
    private String createTicketPage;

    @ValueMapValue
    private String brandTitle;

    @ValueMapValue
    private String brandSubtitle;

    @ValueMapValue
    private String userRole;

    @ValueMapValue
    private String userDisplayName;

    private String currentUserId;
    private Page currentPage;
    private List<AppShellNavItem> workspaceNav;
    private List<AppShellNavItem> manageNav;
    private int totalCount;
    private int assignedToMeCount;
    private int createdByMeCount;

    @PostConstruct
    protected void init() {
        currentUserId = StringUtils.defaultString(resourceResolver.getUserID(), "anonymous");
        workspaceNav = new ArrayList<>();
        manageNav = new ArrayList<>();

        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Resource currentResource = request != null ? request.getResource() : null;
        if (pageManager != null && currentResource != null) {
            currentPage = pageManager.getContainingPage(currentResource);
        }

        if (ticketRepository != null && resourceResolver != null) {
            loadCounts();
        }

        applyDefaults();
        buildNavigation();
    }

    private void applyDefaults() {
        brandTitle = StringUtils.defaultIfBlank(brandTitle, "STMS");
        brandSubtitle = StringUtils.defaultIfBlank(brandSubtitle, "Support Ticket System");
        userRole = StringUtils.defaultIfBlank(userRole, "Support agent");
    }

    private void loadCounts() {
        List<TicketModel> allTickets = ticketRepository.findAllTickets(resourceResolver);
        totalCount = allTickets.size();

        for (TicketModel ticket : allTickets) {
            if (StringUtils.equals(currentUserId, ticket.getAssignee())) {
                assignedToMeCount++;
            }
            if (StringUtils.equals(currentUserId, ticket.getCreatedBy())) {
                createdByMeCount++;
            }
        }
    }

    private void buildNavigation() {
        String listUrl = pageUrl(ticketsListPage);
        boolean onListPage = isCurrentPage(ticketsListPage);
        String assigneeFilter = request != null ? request.getParameter(TicketListModel.PARAM_ASSIGNEE) : null;
        String creatorFilter = request != null ? request.getParameter(TicketListModel.PARAM_CREATOR) : null;

        workspaceNav.add(new AppShellNavItem(
            "All tickets",
            listUrl,
            totalCount,
            onListPage && StringUtils.isBlank(assigneeFilter) && StringUtils.isBlank(creatorFilter),
            true
        ));

        workspaceNav.add(new AppShellNavItem(
            "Assigned to me",
            appendQuery(listUrl, TicketListModel.PARAM_ASSIGNEE, currentUserId),
            assignedToMeCount,
            onListPage && StringUtils.equals(assigneeFilter, currentUserId),
            true
        ));

        workspaceNav.add(new AppShellNavItem(
            "Created by me",
            appendQuery(listUrl, TicketListModel.PARAM_CREATOR, currentUserId),
            createdByMeCount,
            onListPage && StringUtils.equals(creatorFilter, currentUserId),
            createdByMeCount > 0
        ));

        workspaceNav.add(new AppShellNavItem(
            "Watching",
            "#",
            0,
            false,
            false
        ));

        manageNav.add(new AppShellNavItem("Projects", "#", 0, false, false));
        manageNav.add(new AppShellNavItem("Reports", "#", 0, false, false));
        manageNav.add(new AppShellNavItem("Settings", "#", 0, false, false));
    }

    public List<AppShellNavItem> getWorkspaceNav() {
        return workspaceNav;
    }

    public List<AppShellNavItem> getManageNav() {
        return manageNav;
    }

    public String getBrandTitle() {
        return brandTitle;
    }

    public String getBrandSubtitle() {
        return brandSubtitle;
    }

    public String getBrandMark() {
        return StringUtils.isNotBlank(brandTitle) ? brandTitle.substring(0, 1).toUpperCase() : "S";
    }

    public String getUserRole() {
        return userRole;
    }

    public String getUserDisplayName() {
        if (StringUtils.isNotBlank(userDisplayName)) {
            return userDisplayName;
        }
        return formatUserId(currentUserId);
    }

    public String getUserInitials() {
        String name = getUserDisplayName();
        if (StringUtils.isBlank(name)) {
            return "?";
        }
        String[] parts = name.split("\\s+");
        if (parts.length >= 2) {
            return ("" + Character.toUpperCase(parts[0].charAt(0))
                + Character.toUpperCase(parts[1].charAt(0)));
        }
        return name.length() >= 2 ? name.substring(0, 2).toUpperCase() : name.toUpperCase();
    }

    public String getCreateTicketUrl() {
        return pageUrl(createTicketPage);
    }

    public boolean hasCreateTicketPage() {
        return StringUtils.isNotBlank(createTicketPage);
    }

    public String getTicketsListUrl() {
        return pageUrl(ticketsListPage);
    }

    public int getTotalCount() {
        return totalCount;
    }

    private boolean isCurrentPage(String pagePath) {
        return currentPage != null && StringUtils.equals(currentPage.getPath(), pagePath);
    }

    private String pageUrl(String pagePath) {
        if (StringUtils.isBlank(pagePath)) {
            return "#";
        }
        return pagePath + ".html";
    }

    private String appendQuery(String url, String param, String value) {
        if (StringUtils.isBlank(url) || StringUtils.isBlank(param) || StringUtils.isBlank(value) || "#".equals(url)) {
            return url;
        }
        return url + "?" + param + "=" + value;
    }

    private String formatUserId(String userId) {
        if (StringUtils.isBlank(userId) || "anonymous".equals(userId)) {
            return "Guest User";
        }
        if (userId.contains("@")) {
            String local = userId.split("@")[0];
            String[] parts = local.split("[.\\-_]");
            StringBuilder builder = new StringBuilder();
            for (String part : parts) {
                if (StringUtils.isNotBlank(part)) {
                    if (builder.length() > 0) {
                        builder.append(' ');
                    }
                    builder.append(Character.toUpperCase(part.charAt(0)));
                    if (part.length() > 1) {
                        builder.append(part.substring(1));
                    }
                }
            }
            return builder.toString();
        }
        return Character.toUpperCase(userId.charAt(0)) + userId.substring(1);
    }
}
