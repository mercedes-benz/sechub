// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

public enum Severity implements Comparable<Severity> {

    INFO(10),

    UNCLASSIFIED(0),

    LOW(20),

    MEDIUM(30),

    HIGH(40),

    CRITICAL(50),

    ;

    private int level;

    private Severity(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

}
