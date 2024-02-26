// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

/**
 * A helper object to render a code callstack element in HTML
 *
 *
 */
public class HTMLScanResultCodeScanEntry {

    String location;

    Integer line;

    Integer column;

    String source;

    String relevantPart;

    Integer callNumber;

    /**
     * Return the call number (means position inside call stack...)
     *
     * @return integer value or <code>null</code>
     */
    public Integer getCallNumber() {
        return callNumber;
    }

    public Integer getLine() {
        return line;
    }

    public Integer getColumn() {
        return column;
    }

    public String getSource() {
        return source;
    }

    public String getLocation() {
        return location;
    }

    public String getRelevantPart() {
        return relevantPart;
    }
}
