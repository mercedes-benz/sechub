// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.spring;

import java.util.Map;
import java.util.SortedSet;

import com.mercedesbenz.sechub.docgen.Generator;
import com.mercedesbenz.sechub.docgen.util.DocGeneratorUtil;
import com.mercedesbenz.sechub.sharedkernel.Profiles;

public class SystemPropertiesJavaLaunchExampleGenerator extends SystemPropertiesDescriptionGenerator implements Generator {

    @Override
    protected void appendStringContent(StringBuilder sb, Map<String, SortedSet<TableRow>> rowMap) {
        sb.append("\nTIP: Next lines will show java launcher properties which MUST be set because");
        sb.append("\n     there are no defaults defined. You have to define those values when not");
        sb.append("\n     starting in mock mode! The example her is generated and will");
        sb.append("\n     always show the current necessary parts.");
        sb.append("\n----");
        sb.append("\n-Dspring.profiles.active=" + Profiles.DEV + ",postgres,real_products");
        for (Map.Entry<String, SortedSet<TableRow>> entries : rowMap.entrySet()) {
            SortedSet<TableRow> table = entries.getValue();

            for (TableRow row : table) {
                if (!row.hasDefaultValue) {
                    sb.append("\n-D").append(row.propertyKey).append("=value");
                }
            }
        }
        sb.append("\n----");

        sb.append("\nTIP: Instead of java system properties you can also define environment entries");
        sb.append("\n     at your launch configuration or your shell and reduce parameter hell:");
        sb.append("\n----");
        sb.append("\nexport SPRING_PROFILES_ACTIVE=" + Profiles.DEV + ",postgres,real_products");
        for (Map.Entry<String, SortedSet<TableRow>> entries : rowMap.entrySet()) {
            SortedSet<TableRow> table = entries.getValue();

            for (TableRow row : table) {
                if (!row.hasDefaultValue) {
                    String envEntryName = DocGeneratorUtil.convertSystemPropertyToEnvironmentVariable(row.propertyKey);
                    sb.append("\nexport ").append(envEntryName).append("=value");
                }
            }
        }
        sb.append("\n----");
    }

}
