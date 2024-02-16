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
    private String text;

    private Severity(int level, String name) {
        this.level = level;
        this.text = name;
    }

    public int getLevel() {
        return level;
    }

    public String getText() {
        return text;
    }

}
