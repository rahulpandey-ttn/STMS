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
package com.ttn.stms.core.tickets.services;

/**
 * Result of a ticket update attempt.
 */
public class TicketUpdateResult {

    private final boolean success;
    private final String ticketId;
    private final String errorMessage;

    private TicketUpdateResult(boolean success, String ticketId, String errorMessage) {
        this.success = success;
        this.ticketId = ticketId;
        this.errorMessage = errorMessage;
    }

    public static TicketUpdateResult success(String ticketId) {
        return new TicketUpdateResult(true, ticketId, null);
    }

    public static TicketUpdateResult failure(String errorMessage) {
        return new TicketUpdateResult(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
