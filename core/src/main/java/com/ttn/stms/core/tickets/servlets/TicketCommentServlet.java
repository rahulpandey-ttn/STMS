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
package com.ttn.stms.core.tickets.servlets;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

import com.ttn.stms.core.tickets.models.TicketCommentsModel;
import com.ttn.stms.core.tickets.models.TicketDetailModel;
import com.ttn.stms.core.tickets.services.TicketCommentCreateRequest;
import com.ttn.stms.core.tickets.services.TicketCommentCreateResult;
import com.ttn.stms.core.tickets.services.TicketRepository;

/**
 * Handles POST submissions from the ticket comments form component.
 */
@Component(service = Servlet.class)
@SlingServletPaths(TicketCommentServlet.SERVLET_PATH)
@ServiceDescription("STMS Add Ticket Comment Servlet")
public class TicketCommentServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;

    static final String SERVLET_PATH = "/bin/stms/ticket/comment";

    @Reference
    private transient TicketRepository ticketRepository;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        TicketCommentCreateRequest createRequest = new TicketCommentCreateRequest();
        createRequest.setTicketId(request.getParameter(TicketCommentsModel.PARAM_TICKET_ID));
        createRequest.setText(request.getParameter(TicketCommentsModel.PARAM_TEXT));
        createRequest.setAuthor(resolveAuthor(request));

        TicketCommentCreateResult result = ticketRepository.addComment(createRequest);

        if (result.isSuccess()) {
            String detailPage = request.getParameter(TicketCommentsModel.PARAM_DETAIL_PAGE);
            if (StringUtils.isNotBlank(detailPage)) {
                String redirectUrl = detailPage + ".html?" + TicketDetailModel.PARAM_TICKET_ID + "="
                    + encode(createRequest.getTicketId()) + "&" + TicketCommentsModel.PARAM_COMMENT_ADDED + "=true";
                response.sendRedirect(redirectUrl);
                return;
            }
            redirectWithError(request, response, "Comment saved but detail page is not configured.");
            return;
        }

        redirectWithError(request, response, result.getErrorMessage());
    }

    private String resolveAuthor(SlingHttpServletRequest request) {
        ResourceResolver resolver = request.getResourceResolver();
        try {
            UserManager userManager = resolver.adaptTo(UserManager.class);
            if (userManager != null && request.getUserPrincipal() != null) {
                Authorizable authorizable = userManager.getAuthorizable(request.getUserPrincipal());
                if (authorizable != null) {
                    String authorId = authorizable.getID();
                    if (StringUtils.isNotBlank(authorId)) {
                        return authorId;
                    }
                }
            }
        } catch (Exception ignored) {
            // fall back to remote user below
        }

        return StringUtils.defaultIfBlank(request.getRemoteUser(), "anonymous");
    }

    private void redirectWithError(SlingHttpServletRequest request, SlingHttpServletResponse response, String error)
            throws IOException {
        String detailPage = request.getParameter(TicketCommentsModel.PARAM_DETAIL_PAGE);
        StringBuilder url = new StringBuilder(detailPage).append(".html?");
        url.append(TicketDetailModel.PARAM_TICKET_ID).append('=').append(encode(request.getParameter(
            TicketCommentsModel.PARAM_TICKET_ID)));
        url.append('&').append(TicketCommentsModel.PARAM_ERROR).append('=').append(encode(error));

        String text = request.getParameter(TicketCommentsModel.PARAM_TEXT);
        if (StringUtils.isNotBlank(text)) {
            url.append('&').append(TicketCommentsModel.PARAM_TEXT).append('=').append(encode(text));
        }

        response.sendRedirect(url.toString());
    }

    private String encode(String value) {
        return URLEncoder.encode(StringUtils.defaultString(value), StandardCharsets.UTF_8);
    }

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "POST required");
    }
}
