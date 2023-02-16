package com.mercedesbenz.sechub.docgen;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.ModuleGroup;
import com.mercedesbenz.sechub.commons.model.ScanType;

public class ModuleGroupToModuleTableGenerator implements Generator {

    public String generate() {
        return createTable();
    }

    private String createTable() {

        StringBuilder sb = new StringBuilder();
        add(sb, "[options=\"header\",cols=\"1,1\"]");
        add(sb, "|===");
        add(sb, "|Module group| Contained modules");
        for (ModuleGroup group : ModuleGroup.values()) {

            List<ScanType> typeList = Arrays.asList(group.getModuleScanTypes());
            StringBuilder entryBuilder = new StringBuilder();
            for (Iterator<ScanType> it = typeList.iterator(); it.hasNext();) {

                ScanType next = it.next();
                entryBuilder.append(next.getId());
                if (it.hasNext()) {
                    entryBuilder.append(", ");
                }
            }

            add(sb, "|" + group.getId() + "   |" + entryBuilder.toString());
        }
        add(sb, "|===");
        return sb.toString();
    }

    private void add(StringBuilder sb, String text) {
        sb.append(text).append("\n");
    }
}
