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
 * Result of a ticket comment creation attempt.
 */
public class TicketCommentCreateResult {

    private final boolean success;
    private final String commentId;
    private final String errorMessage;

    private TicketCommentCreateResult(boolean success, String commentId, String errorMessage) {
        this.success = success;
        this.commentId = commentId;
        this.errorMessage = errorMessage;
    }

    public static TicketCommentCreateResult success(String commentId) {
        return new TicketCommentCreateResult(true, commentId, null);
    }

    public static TicketCommentCreateResult failure(String errorMessage) {
        return new TicketCommentCreateResult(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
