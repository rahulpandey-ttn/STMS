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

/**
 * Allowed ticket priority values stored on JCR nodes.
 */
public enum TicketPriority {

    LOW("low", "Low"),
    MEDIUM("medium", "Medium"),
    HIGH("high", "High"),
    CRITICAL("critical", "Critical");

    private final String value;
    private final String label;

    TicketPriority(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Resolves a JCR property value to a {@link TicketPriority}, or {@code null} if unknown.
     */
    public static TicketPriority fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (TicketPriority priority : values()) {
            if (priority.value.equals(value)) {
                return priority;
            }
        }
        return null;
    }
}
