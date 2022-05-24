package com.mercedesbenz.sechub.integrationtest.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TemplateData {

    private List<String> referenceIds = new ArrayList<>();
    private Map<String, String> variables = new TreeMap<>();

    private TemplateData() {

    }

    /**
     * @return unmodifiable list of reference ids
     */
    public List<String> getReferenceIds() {
        return Collections.unmodifiableList(referenceIds);
    }

    /**
     * @return unmodifiable map of variables
     */
    public Map<String, String> getVariables() {
        return Collections.unmodifiableMap(variables);
    }

    public static TemplateDataBuilder builder() {
        return new TemplateDataBuilder();
    }

    public static class TemplateDataBuilder {

        private TemplateData data;

        public TemplateDataBuilder() {
            this.data = new TemplateData();
        }

        public TemplateDataBuilder addReferenceId(String id) {
            data.referenceIds.add(id);
            return this;
        }

        public TemplateDataBuilder setVariable(String name, String value) {
            data.variables.put(name, value);
            return this;
        }

        public TemplateData build() {
            return data;
        }
    }
}