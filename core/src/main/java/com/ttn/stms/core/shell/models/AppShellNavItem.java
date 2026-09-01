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

/**
 * Navigation item for the STMS app shell sidebar.
 */
public class AppShellNavItem {

    private final String label;
    private final String href;
    private final int count;
    private final boolean active;
    private final boolean showCount;

    public AppShellNavItem(String label, String href, int count, boolean active, boolean showCount) {
        this.label = label;
        this.href = href;
        this.count = count;
        this.active = active;
        this.showCount = showCount;
    }

    public String getLabel() {
        return label;
    }

    public String getHref() {
        return href;
    }

    public int getCount() {
        return count;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isShowCount() {
        return showCount;
    }
}
