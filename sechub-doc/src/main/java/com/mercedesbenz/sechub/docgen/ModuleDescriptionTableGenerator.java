// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen;

import com.mercedesbenz.sechub.commons.model.ScanType;

public class ModuleDescriptionTableGenerator implements Generator {

    public String generate() {
        return createTable();
    }

    private String createTable() {

        StringBuilder sb = new StringBuilder();
        add(sb, "[options=\"header\",cols=\"1,1\"]");
        add(sb, "|===");
        add(sb, "|Module | Description");
        /* internal a module is represented by a scan type ... */
        for (ScanType scanType : ScanType.values()) {
            if (!scanType.isInternalScanType()) {
                add(sb, "|" + scanType.getId() + "   |" + scanType.getDescription());
            }
        }
        add(sb, "|===");

        return sb.toString();
    }

    private void add(StringBuilder sb, String text) {
        sb.append(text).append("\n");
    }
}
