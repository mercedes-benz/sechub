// SPDX-License-Identifier: MIT
package com.daimler.sechub.adapter.checkmarx;

public enum CheckmarxState {
    COMPLETE("completed"), CANCELED("canceled");

    private String id;

    private CheckmarxState(String id) {
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
        for (CheckmarxState value : values()) {
            if (value.isRepresentedBy(state)) {
                return true;
            }
        }
        return false;
    }
}
