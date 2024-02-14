// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model;

public enum Severity implements Comparable<Severity> {

    INFO(10, "Info"),

    UNCLASSIFIED(0, "Unclassified"),

    LOW(20, "Low"),

    MEDIUM(30, "Medium"),

    HIGH(40, "High"),

    CRITICAL(50, "Critical"),

    ;

    private int level;
    private String name;

    private Severity(int level, String name) {
        this.level = level;
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Resolves human readable name. Is different to name() method
     *
     * @return a human readable name - e.g. CRITCIAL -> "Critical"
     */
    public String getName() {
        return name;
    }

}
