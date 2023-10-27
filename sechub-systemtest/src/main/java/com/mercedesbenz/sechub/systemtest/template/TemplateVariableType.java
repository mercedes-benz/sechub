// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.template;

public enum TemplateVariableType {
    ENV("env"),

    SECRET_ENV("secretEnv"),

    USER_VARIABLES("variables"),

    RUNTIME_VARIABLES("runtime"),

    ;

    private String prefix;
    private String fullPrefix;

    private TemplateVariableType(String id) {
        this.prefix = id;
        this.fullPrefix = id + ".";
    }

    public String getFullPrefix() {
        return fullPrefix;
    }

    public String getPrefix() {
        return prefix;
    }

}