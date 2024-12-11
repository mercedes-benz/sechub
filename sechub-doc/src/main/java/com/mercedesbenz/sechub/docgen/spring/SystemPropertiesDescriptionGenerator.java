// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.docgen.spring;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.mercedesbenz.sechub.commons.core.environment.SecureEnvironmentVariableKeyValueRegistry;
import com.mercedesbenz.sechub.commons.core.environment.SecureEnvironmentVariableKeyValueRegistry.EnvironmentVariableKeyValueEntry;
import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;
import com.mercedesbenz.sechub.docgen.DocAnnotationData;
import com.mercedesbenz.sechub.docgen.Generator;
import com.mercedesbenz.sechub.docgen.spring.SpringValueExtractor.SpringValue;
import com.mercedesbenz.sechub.docgen.spring.SpringValueFilter.AcceptAllSpringValueFilter;

public class SystemPropertiesDescriptionGenerator implements Generator {

    SpringValueExtractor springValueExtractor;

    public SystemPropertiesDescriptionGenerator() {
        this.springValueExtractor = new SpringValueExtractor();
    }

    public String generate(List<DocAnnotationData> list, SecureEnvironmentVariableKeyValueRegistry registry) {
        return generate(list, registry, AcceptAllSpringValueFilter.INSTANCE, null);
    }

    public String generate(List<DocAnnotationData> list, SecureEnvironmentVariableKeyValueRegistry registry,
            Map<String, SortedSet<TableRow>> customPropertiesMap) {
        return generate(list, registry, AcceptAllSpringValueFilter.INSTANCE, customPropertiesMap);
    }

    protected String generate(List<DocAnnotationData> list, SecureEnvironmentVariableKeyValueRegistry registry, SpringValueFilter filter,
            Map<String, SortedSet<TableRow>> customPropertiesMap) {
        if (list == null || list.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Map<String, SortedSet<TableRow>> rowMap = new TreeMap<>();

        if (customPropertiesMap != null) {
            rowMap.putAll(customPropertiesMap);
        }

        for (DocAnnotationData data : list) {
            if (SimpleStringUtils.isEmpty(data.springValue)) {
                continue;
            }

            SpringValue extracted = springValueExtractor.extract(data.springValue);
            if (filter.isFiltered(extracted)) {
                continue;
            }
            String mustBeEnvironmentVariableName = null;
            for (EnvironmentVariableKeyValueEntry entry : registry.getEntries()) {
                if (Objects.equals(extracted.getKey(), entry.getKey())) {
                    mustBeEnvironmentVariableName = entry.getVariableName();
                    break;
                }
            }
            TableRow row = new TableRow();
            row.defaultValue = extracted.getDefaultValue();
            row.hasDefaultValue = extracted.hasDefaultValue();
            row.propertyKey = mustBeEnvironmentVariableName == null ? extracted.getKey() : mustBeEnvironmentVariableName;
            row.description = data.description + (mustBeEnvironmentVariableName == null ? "" : " *This must be defined as an environment variable!*");

            SortedSet<TableRow> rows = rowMap.get(data.scope);
            if (rows == null) {
                rows = new TreeSet<>();
                rowMap.put(data.scope, rows);
            }
            rows.add(row);
        }
        if (rowMap.isEmpty()) {
            return "";
        }
        appendStringContent(sb, rowMap);
        return sb.toString();
    }

    protected void appendStringContent(StringBuilder sb, Map<String, SortedSet<TableRow>> rowMap) {
        for (Map.Entry<String, SortedSet<TableRow>> entries : rowMap.entrySet()) {
            SortedSet<TableRow> table = entries.getValue();
            sb.append("[[section-gen-configuration-scope-").append(entries.getKey()).append("]]\n");
            sb.append("[options=\"header\",cols=\"1,1,1\"]\n");
            sb.append(".").append(buildTitle(entries.getKey()));
            sb.append("\n|===\n");
            sb.append("|Key or variable name   |Default   |Description \n");
            sb.append("//----------------------\n");
            for (TableRow row : table) {
                sb.append("|").append(row.propertyKey);
                sb.append("|").append(row.defaultValue);
                sb.append("|").append(row.description);
                sb.append("\n");
            }
            sb.append("\n|===\n\n");
        }
    }

    private String buildTitle(String key) {
        return "Scope '" + key + "'";
    }

    public static class TableRow implements Comparable<TableRow> {
        String propertyKey;
        String defaultValue;
        String description;
        boolean hasDefaultValue;

        public TableRow() {
        }

        public TableRow(String propertyKey, String defaultValue, String description, boolean hasDefaultValue) {
            this.propertyKey = propertyKey;
            this.defaultValue = defaultValue;
            this.description = description;
            this.hasDefaultValue = hasDefaultValue;
        }

        @Override
        public int compareTo(TableRow o) {
            return getPropertyKey().compareTo(o.getPropertyKey());
        }

        public String getPropertyKey() {
            if (propertyKey == null) {
                return "";
            }
            return propertyKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
            result = prime * result + ((description == null) ? 0 : description.hashCode());
            result = prime * result + ((propertyKey == null) ? 0 : propertyKey.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TableRow other = (TableRow) obj;
            if (defaultValue == null) {
                if (other.defaultValue != null)
                    return false;
            } else if (!defaultValue.equals(other.defaultValue))
                return false;
            if (description == null) {
                if (other.description != null)
                    return false;
            } else if (!description.equals(other.description))
                return false;
            if (propertyKey == null) {
                if (other.propertyKey != null)
                    return false;
            } else if (!propertyKey.equals(other.propertyKey))
                return false;
            return true;
        }
    }
}
