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
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

import com.ttn.stms.core.tickets.models.TicketCreateModel;
import com.ttn.stms.core.tickets.models.TicketDetailModel;
import com.ttn.stms.core.tickets.services.TicketCreateRequest;
import com.ttn.stms.core.tickets.services.TicketCreateResult;
import com.ttn.stms.core.tickets.services.TicketRepository;

/**
 * Handles POST submissions from the create-ticket form component.
 */
@Component(service = Servlet.class)
@SlingServletPaths(TicketCreateServlet.SERVLET_PATH)
@ServiceDescription("STMS Create Ticket Servlet")
public class TicketCreateServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;

    static final String SERVLET_PATH = "/bin/stms/ticket/create";

    @Reference
    private transient TicketRepository ticketRepository;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        TicketCreateRequest createRequest = new TicketCreateRequest();
        createRequest.setTitle(request.getParameter(TicketCreateModel.PARAM_TITLE));
        createRequest.setDescription(request.getParameter(TicketCreateModel.PARAM_DESCRIPTION));
        createRequest.setPriority(request.getParameter(TicketCreateModel.PARAM_PRIORITY));
        createRequest.setAssignee(request.getParameter(TicketCreateModel.PARAM_ASSIGNEE));

        TicketCreateResult result = ticketRepository.createTicket(createRequest);

        if (result.isSuccess()) {
            String detailPage = request.getParameter(TicketCreateModel.PARAM_DETAIL_PAGE);
            if (StringUtils.isNotBlank(detailPage)) {
                String redirectUrl = detailPage + ".html?" + TicketDetailModel.PARAM_TICKET_ID + "="
                    + encode(result.getTicketId()) + "&created=true";
                response.sendRedirect(redirectUrl);
                return;
            }
            redirectWithError(request, response, "Ticket created but detail page is not configured.");
            return;
        }

        redirectWithError(request, response, result.getErrorMessage());
    }

    private void redirectWithError(SlingHttpServletRequest request, SlingHttpServletResponse response, String error)
            throws IOException {
        String redirectUrl = buildErrorRedirect(request, error);
        response.sendRedirect(redirectUrl);
    }

    private String buildErrorRedirect(SlingHttpServletRequest request, String error) {
        String formPage = request.getParameter(TicketCreateModel.PARAM_FORM_PAGE);
        StringBuilder url = new StringBuilder(formPage).append(".html?");
        url.append(TicketCreateModel.PARAM_ERROR).append('=').append(encode(error));

        appendParam(url, TicketCreateModel.PARAM_TITLE, request.getParameter(TicketCreateModel.PARAM_TITLE));
        appendParam(url, TicketCreateModel.PARAM_DESCRIPTION, request.getParameter(TicketCreateModel.PARAM_DESCRIPTION));
        appendParam(url, TicketCreateModel.PARAM_PRIORITY, request.getParameter(TicketCreateModel.PARAM_PRIORITY));
        appendParam(url, TicketCreateModel.PARAM_ASSIGNEE, request.getParameter(TicketCreateModel.PARAM_ASSIGNEE));

        return url.toString();
    }

    private void appendParam(StringBuilder url, String name, String value) {
        if (StringUtils.isNotBlank(value)) {
            url.append('&').append(name).append('=').append(encode(value));
        }
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
