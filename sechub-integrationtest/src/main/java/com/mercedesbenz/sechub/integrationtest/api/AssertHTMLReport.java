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
    private String htmlLocation;

    public static AssertHTMLReport assertHTMLReport(String html) {
        return new AssertHTMLReport(html, "HTML from memory");
    }

    public static AssertHTMLReport assertHTMLReport(String html, String filePath) {
        return new AssertHTMLReport(html, "HTML from file: " + filePath);
    }

    private AssertHTMLReport(String html, String htmlLocation) {
        assertNotNull("Report may not be null", html);
        assertNotNull("HTML location may not be null", htmlLocation);

        this.html = html;
        this.htmlLocation = htmlLocation;

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
        fail(htmlLocation + " -> " + message + "\n" + html);
    }

    public AssertHTMLReport hasMetaDataLabel(String key, String value) {
        String keyPart = "<td class=\"metaDataLabelKey\">" + key + "</td>";
        if (!html.contains(keyPart)) {
            failWithDump("The report does not contain expected key part:" + keyPart);
        }

        String valuePart = "<td class=\"metaDataLabelValue\">" + value + "</td>";
        if (!html.contains(valuePart)) {
            failWithDump("The report does not contain expected value part:" + valuePart);
        }
        return this;
    }

    public AssertHTMLReport hasHTMLString(String value) {
        if (!html.contains(value)) {
            failWithDump("The report does not contain expected HTML string ':" + value + "'");
        }
        return this;
    }
}
