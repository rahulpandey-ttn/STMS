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
package com.ttn.stms.it.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.junit.rules.CQAuthorClassRule;
import com.adobe.cq.testing.junit.rules.CQRule;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Integration test: POST {@code /bin/stms/ticket/create} on author and verify JCR node.
 *
 * <p><b>Prerequisites</b> (local author {@code localhost:4502}):
 * <ul>
 *   <li>{@code mvn clean install -PautoInstallSinglePackage} (or at least {@code core},
 *       {@code ui.config}, {@code ui.content})</li>
 *   <li>{@code /content/stms/tickets} exists</li>
 *   <li>Service user {@code stms-ticket-service} and subservice {@code stms-ticket-write} configured</li>
 * </ul>
 *
 * <p><b>Run:</b>
 * <pre>{@code
 * mvn clean verify -pl it.tests -Plocal
 * }</pre>
 */
public class TicketCreateIT {

    private static final String CREATE_SERVLET_PATH = "/bin/stms/ticket/create";

    private static final String TICKETS_ROOT = "/content/stms/tickets";

    private static final String DETAIL_PAGE = "/content/stms/us/en/ticket-detail";

    private static final String FORM_PAGE = "/content/stms/us/en/tickets/create-ticket";

    private static final Pattern TICKET_ID_IN_LOCATION = Pattern.compile("ticketId=([^&]+)");

    @ClassRule
    public static final CQAuthorClassRule cqBaseClassRule = new CQAuthorClassRule();

    @Rule
    public CQRule cqBaseRule = new CQRule(cqBaseClassRule.authorRule);

    private static CQClient adminAuthor;

    private String createdTicketPath;

    @BeforeClass
    public static void beforeClass() {
        adminAuthor = cqBaseClassRule.authorRule.getAdminClient(CQClient.class);
    }

    @After
    public void cleanup() throws ClientException {
        if (createdTicketPath != null) {
            try {
                adminAuthor.doDelete(createdTicketPath + ".json", Collections.emptyList(), Collections.emptyList(), HttpStatus.SC_OK);
            } catch (ClientException ignored) {
                // Best-effort cleanup; node may already be removed
            }
            createdTicketPath = null;
        }
    }

    @Test
    public void testCreateTicketViaServlet() throws Exception {
        String uniqueTitle = "IT Ticket " + System.currentTimeMillis();

        SlingHttpResponse response = adminAuthor.doPost(
            CREATE_SERVLET_PATH,
            FormEntityBuilder.create()
                .addParameter("title", uniqueTitle)
                .addParameter("description", "Created by TicketCreateIT integration test.")
                .addParameter("priority", "medium")
                .addParameter("assignee", "it-tester@ttn.com")
                .addParameter("detailPage", DETAIL_PAGE)
                .addParameter("formPage", FORM_PAGE)
                .build(),
            HttpStatus.SC_MOVED_TEMPORARILY
        );

        String location = response.getFirstHeader(HttpHeaders.LOCATION).getValue();
        assertNotNull("Expected redirect Location header", location);
        assertTrue("Redirect should target detail page", location.contains("/ticket-detail.html"));

        String ticketId = extractTicketId(location);
        assertNotNull("ticketId query param missing from redirect", ticketId);
        assertTrue("ticketId should match TICKET-NNNN", ticketId.startsWith("TICKET-"));

        createdTicketPath = TICKETS_ROOT + "/" + ticketId;

        JsonNode ticketJson = adminAuthor.doGetJson(createdTicketPath + ".json", HttpStatus.SC_OK);
        assertEquals(uniqueTitle, ticketJson.get("title").asText());
        assertEquals("open", ticketJson.get("status").asText());
        assertEquals("medium", ticketJson.get("priority").asText());
        assertEquals("stms/tickets/ticket", ticketJson.get("sling:resourceType").asText());

        JsonNode commentsJson = adminAuthor.doGetJson(createdTicketPath + "/comments.json", HttpStatus.SC_OK);
        assertEquals("stms/tickets/comments", commentsJson.get("sling:resourceType").asText());
    }

    private static String extractTicketId(String location) {
        Matcher matcher = TICKET_ID_IN_LOCATION.matcher(location);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
