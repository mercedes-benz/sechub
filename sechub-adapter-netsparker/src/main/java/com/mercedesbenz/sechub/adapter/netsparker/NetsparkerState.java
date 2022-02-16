// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.netsparker;

public enum NetsparkerState {
    COMPLETE("Complete"),

    FAILED("Failed"),

    CANCELED("Cancelled");

    private String id;

    private NetsparkerState(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id may not be null!");
        }
        this.id = id;
    }

    public boolean isRepresentedBy(String state) {
        if (state == null) {
            return false;
        }
        return id.equals(state);
    }

    public static boolean isWellknown(String state) {
        for (NetsparkerState value : values()) {
            if (value.isRepresentedBy(state)) {
                return true;
            }
        }
        return false;
    }
}
