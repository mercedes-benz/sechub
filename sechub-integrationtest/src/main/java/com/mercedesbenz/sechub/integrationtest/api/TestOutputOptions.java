// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

public class TestOutputOptions {

    private boolean withReport = true;
    private boolean withOutput = true;
    private boolean withError = true;
    private boolean withStatus = true;
    private boolean withMessages = true;

    private TestOutputOptions() {
    }

    public boolean isWithReport() {
        return withReport;
    }

    public boolean isWithError() {
        return withError;
    }

    public boolean isWithOutput() {
        return withOutput;
    }

    public boolean isWithStatus() {
        return withStatus;
    }

    public boolean isWithMessages() {
        return withMessages;
    }

    public TestOutputOptions withoutReport() {
        this.withReport = false;
        return this;
    }

    public TestOutputOptions withoutOutput() {
        this.withOutput = false;
        return this;
    }

    public TestOutputOptions withoutError() {
        this.withError = false;
        return this;
    }

    public TestOutputOptions withoutStatus() {
        this.withStatus = false;
        return this;
    }

    public TestOutputOptions withoutMessages() {
        this.withMessages = false;
        return this;
    }

    /**
     * Creates output options with all enabled
     *
     * @return options
     */
    public static TestOutputOptions create() {
        return new TestOutputOptions();
    }

}