// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import static org.junit.Assert.*;

/**
 * An extreme simple implemented HTML assertion.
 *
 * @author Albert Tregnaghi
 *
 */
public class AssertHTMLReport {

    private String html;

    public static AssertHTMLReport assertHTMLReport(String html) {
        return new AssertHTMLReport(html);
    }

    private AssertHTMLReport(String html) {
        assertNotNull("Report may not be null", html);

        this.html = html;
        if (!html.contains("<html")) {
            failWithDump("The report must at least contain a HTML start tag");
        }

    }

    public AssertHTMLReport containsAtLeastOneOpenDetailsBlock() {
        if (!html.contains("data-open=\"Open details\"")) {
            failWithDump("Not at least one Open details block found!");
        }

        return this;
    }

    private void failWithDump(String message) {
        fail(message + "\n" + html);
    }

}
