package com.mercedesbenz.sechub.owaspzapwrapper.helper;

public enum OwaspZapURLType {
    INCLUDE("include"),

    EXCLUDE("exclude"),

    ;

    private String id;

    private OwaspZapURLType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
