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
package com.ttn.stms.core.tickets.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TicketStatusTest {

    @Test
    void testFromValueKnownStatuses() {
        assertEquals(TicketStatus.OPEN, TicketStatus.fromValue("open"));
        assertEquals(TicketStatus.IN_PROGRESS, TicketStatus.fromValue("in-progress"));
        assertEquals(TicketStatus.RESOLVED, TicketStatus.fromValue("resolved"));
        assertEquals(TicketStatus.CLOSED, TicketStatus.fromValue("closed"));
    }

    @Test
    void testFromValueUnknownOrNull() {
        assertNull(TicketStatus.fromValue("pending"));
        assertNull(TicketStatus.fromValue(null));
    }

    @Test
    void testLabels() {
        assertEquals("In Progress", TicketStatus.IN_PROGRESS.getLabel());
        assertEquals("in-progress", TicketStatus.IN_PROGRESS.getValue());
    }
}
