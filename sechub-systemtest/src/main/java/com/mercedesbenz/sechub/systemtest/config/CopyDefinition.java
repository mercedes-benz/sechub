// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.config;

public class CopyDefinition extends AbstractDefinition {

    private String from;

    private String to;

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
