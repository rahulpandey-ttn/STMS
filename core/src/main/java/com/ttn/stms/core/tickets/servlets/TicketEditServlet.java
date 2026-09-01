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

import com.ttn.stms.core.tickets.models.TicketDetailModel;
import com.ttn.stms.core.tickets.models.TicketEditModel;
import com.ttn.stms.core.tickets.services.TicketRepository;
import com.ttn.stms.core.tickets.services.TicketUpdateRequest;
import com.ttn.stms.core.tickets.services.TicketUpdateResult;

/**
 * Handles POST submissions from the edit-ticket form component.
 */
@Component(service = Servlet.class)
@SlingServletPaths(TicketEditServlet.SERVLET_PATH)
@ServiceDescription("STMS Update Ticket Servlet")
public class TicketEditServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;

    static final String SERVLET_PATH = "/bin/stms/ticket/update";

    @Reference
    private transient TicketRepository ticketRepository;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        TicketUpdateRequest updateRequest = new TicketUpdateRequest();
        updateRequest.setTicketId(request.getParameter(TicketEditModel.PARAM_TICKET_ID));
        updateRequest.setTitle(request.getParameter(TicketEditModel.PARAM_TITLE));
        updateRequest.setDescription(request.getParameter(TicketEditModel.PARAM_DESCRIPTION));
        updateRequest.setStatus(request.getParameter(TicketEditModel.PARAM_STATUS));
        updateRequest.setPriority(request.getParameter(TicketEditModel.PARAM_PRIORITY));
        updateRequest.setAssignee(request.getParameter(TicketEditModel.PARAM_ASSIGNEE));

        TicketUpdateResult result = ticketRepository.updateTicket(updateRequest);

        if (result.isSuccess()) {
            String detailPage = request.getParameter(TicketEditModel.PARAM_DETAIL_PAGE);
            if (StringUtils.isNotBlank(detailPage)) {
                String redirectUrl = detailPage + ".html?" + TicketDetailModel.PARAM_TICKET_ID + "="
                    + encode(result.getTicketId()) + "&" + TicketDetailModel.PARAM_UPDATED + "=true";
                response.sendRedirect(redirectUrl);
                return;
            }
            redirectWithError(request, response, "Ticket updated but detail page is not configured.");
            return;
        }

        redirectWithError(request, response, result.getErrorMessage());
    }

    private void redirectWithError(SlingHttpServletRequest request, SlingHttpServletResponse response, String error)
            throws IOException {
        String formPage = request.getParameter(TicketEditModel.PARAM_FORM_PAGE);
        StringBuilder url = new StringBuilder(formPage).append(".html?");
        url.append(TicketEditModel.PARAM_TICKET_ID).append('=').append(encode(request.getParameter(
            TicketEditModel.PARAM_TICKET_ID)));
        url.append('&').append(TicketEditModel.PARAM_ERROR).append('=').append(encode(error));

        appendParam(url, TicketEditModel.PARAM_TITLE, request.getParameter(TicketEditModel.PARAM_TITLE));
        appendParam(url, TicketEditModel.PARAM_DESCRIPTION, request.getParameter(TicketEditModel.PARAM_DESCRIPTION));
        appendParam(url, TicketEditModel.PARAM_STATUS, request.getParameter(TicketEditModel.PARAM_STATUS));
        appendParam(url, TicketEditModel.PARAM_PRIORITY, request.getParameter(TicketEditModel.PARAM_PRIORITY));
        appendParam(url, TicketEditModel.PARAM_ASSIGNEE, request.getParameter(TicketEditModel.PARAM_ASSIGNEE));

        response.sendRedirect(url.toString());
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
