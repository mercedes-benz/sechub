package com.mercedesbenz.sechub.domain.scan.report;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.Severity;

public class HTMLFirstLinkToSeveritySupport {

    public static HTMLFirstLinkToSeveritySupport DEFAULT = new HTMLFirstLinkToSeveritySupport();

    public String createLinkToFirstOf(ScanType scanType, Severity severity) {
        return "#" + createAnkerFirstOf(scanType, severity);
    }

    public String createAnkerFirstOf(ScanType scanType, Severity severity) {
        String anker = "first_" + scanType + "_" + severity;
        return anker.toLowerCase();
    }
}
