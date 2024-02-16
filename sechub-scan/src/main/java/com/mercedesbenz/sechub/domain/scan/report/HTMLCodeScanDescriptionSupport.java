// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.SecHubCodeCallStack;
import com.mercedesbenz.sechub.commons.model.SecHubFinding;

/**
 * This class builds descriptions suitable for HTML output
 *
 * @author Albert Tregnaghi
 *
 */
public class HTMLCodeScanDescriptionSupport {

    public boolean isCodeScan(SecHubFinding finding) {
        SecHubCodeCallStack code = finding.getCode();
        return code != null;
    }

    public List<HTMLScanResultCodeScanEntry> buildEntries(SecHubFinding finding) {
        if (finding == null) {
            return Collections.emptyList();
        }

        SecHubCodeCallStack code = finding.getCode();
        if (code == null) {
            return Collections.emptyList();
        }

        int callNumber = 1;
        List<HTMLScanResultCodeScanEntry> descriptionList = new ArrayList<>();
        descriptionList.add(createEntry(callNumber++, code));

        SecHubCodeCallStack lastCode = code;
        while (lastCode.getCalls() != null) {
            lastCode = lastCode.getCalls();
            descriptionList.add(createEntry(callNumber++, lastCode));
        }

        return descriptionList;
    }

    private HTMLScanResultCodeScanEntry createEntry(int callNumber, SecHubCodeCallStack code) {
        requireNonNull(code, "Code call stack may not be null!");

        HTMLScanResultCodeScanEntry entry = new HTMLScanResultCodeScanEntry();

        entry.callNumber = callNumber;
        entry.column = code.getColumn();
        entry.line = code.getLine();
        entry.location = code.getLocation();
        entry.relevantPart = code.getRelevantPart();

        String source = code.getSource();
        if (source != null) {
            entry.source = source.trim();// to improve HTML report readability we do trim leading spaces...
        }

        return entry;
    }
}
