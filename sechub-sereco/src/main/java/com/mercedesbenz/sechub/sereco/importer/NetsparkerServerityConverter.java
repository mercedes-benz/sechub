// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import com.mercedesbenz.sechub.sereco.metadata.SerecoSeverity;

// https://www.netsparker.com/support/vulnerability-severity-levels-netsparker/
//Critical
//High
//Medium
//Low
// funny: these are not the real values in xml
//https://www.netsparker.com/blog/docs-and-faqs/sample-xml-report-vulnerability-mapping-scanner/
public enum NetsparkerServerityConverter {

    NONE(SerecoSeverity.INFO),

    LOW(SerecoSeverity.LOW),

    MEDIUM(SerecoSeverity.MEDIUM),

    HIGH(SerecoSeverity.HIGH),

    IMPORTANT(SerecoSeverity.CRITICAL),

    ;

    private SerecoSeverity severity;

    private NetsparkerServerityConverter(SerecoSeverity severity) {
        this.severity = severity;
    }

    public static SerecoSeverity convert(String severity) {
        if (severity == null) {
            return SerecoSeverity.UNCLASSIFIED;
        }
        String upperCased = severity.toUpperCase();
        for (NetsparkerServerityConverter netsparkerSeverity : values()) {
            if (netsparkerSeverity.name().contentEquals(upperCased)) {
                return netsparkerSeverity.severity;
            }
        }
        return SerecoSeverity.UNCLASSIFIED;
    }
}
