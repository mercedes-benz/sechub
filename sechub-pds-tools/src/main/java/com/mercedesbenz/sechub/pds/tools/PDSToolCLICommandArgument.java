// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools;

public class PDSToolCLICommandArgument {

    private String name;
    private String description;
    private boolean optional;

    public PDSToolCLICommandArgument(String name, String description) {
        this(name, description, false);
    }

    public PDSToolCLICommandArgument(String name, String description, boolean optional) {
        this.name = name;
        this.description = description;
        this.optional = optional;
    }

    public boolean isOptional() {
        return optional;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
