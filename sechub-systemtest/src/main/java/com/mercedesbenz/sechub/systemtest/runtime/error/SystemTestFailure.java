// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime.error;

public class SystemTestFailure {

    private String message;
    private String details;

    public SystemTestFailure() {
    }

    public SystemTestFailure(String message, String details) {
        this.message = message;
        this.details = details;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setDetails(String description) {
        this.details = description;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "SystemTestError [" + (message != null ? "\nmessage=" + message + ", " : "") + (details != null ? "\ndetails=" + details : "") + "]";
    }

}
