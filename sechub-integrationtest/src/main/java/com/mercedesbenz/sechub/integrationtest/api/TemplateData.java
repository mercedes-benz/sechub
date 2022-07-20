// SPDX-License-Identifier: MIT
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

        /**
         * Adds a references id. Inside the templates we have "__use1__", "__use2__"
         * etc. as variables. Those variables will be replaced with the added reference
         * IDs: The first added reference id will be used for "__use1__". The second
         * added reference id will be used for "__use2__" etc. etc.
         *
         * @param id
         * @return builder
         */
        public TemplateDataBuilder addReferenceId(String id) {
            data.referenceIds.add(id);
            return this;
        }

        /**
         * Set a variable inside a template. E.g. when using "__folder__" as name and
         * "myFolder1" as value, the content of the template having "__folder__" inside
         * will be replaced with "myFolder1".
         *
         * @param name  name of the variable
         * @param value content of the variable
         * @return builder
         */
        public TemplateDataBuilder setVariable(String name, String value) {
            data.variables.put(name, value);
            return this;
        }

        public TemplateData build() {
            return data;
        }
    }
}