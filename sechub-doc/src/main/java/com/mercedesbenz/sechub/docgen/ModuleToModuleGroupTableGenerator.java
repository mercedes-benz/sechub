// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen;

import com.mercedesbenz.sechub.commons.model.ModuleGroup;
import com.mercedesbenz.sechub.commons.model.ScanType;

public class ModuleToModuleGroupTableGenerator implements Generator {

    public String generate() {
        return createTable();
    }

    private String createTable() {

        StringBuilder sb = new StringBuilder();
        add(sb, "[options=\"header\",cols=\"1,1\"]");
        add(sb, "|===");
        add(sb, "|Module | Module group");
        for (ScanType scanType : ScanType.values()) {
            for (ModuleGroup group : ModuleGroup.values()) {
                if (group.isGivenModuleInGroup(scanType)) {
                    add(sb, "|" + scanType.getId() + "   |" + group.getId());
                }
            }
        }
        add(sb, "|===");
        return sb.toString();
    }

    private void add(StringBuilder sb, String text) {
        sb.append(text).append("\n");
    }
}
