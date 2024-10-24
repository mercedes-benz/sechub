// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.template;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.JSONable;

public class TemplateDefinition implements JSONable<TemplateDefinition> {

    private static TemplateDefinition IMPORTER = new TemplateDefinition();

    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_ASSETS = "assets";
    public static final String PROPERTY_VARIABLES = "variables";

    private TemplateType type;

    private List<String> assets = new ArrayList<>();
    private List<TemplateVariable> variables = new ArrayList<>();

    private String id;

    public static TemplateDefinition from(String json) {
        return IMPORTER.fromJSON(json);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<String> getAssets() {
        return assets;
    }

    public List<TemplateVariable> getVariables() {
        return variables;
    }

    public void setType(TemplateType type) {
        this.type = type;
    }

    public TemplateType getType() {
        return type;
    }

    public static class TemplateVariable {
        public static final String PROPERTY_NAME = "name";
        public static final String PROPERTY_OPTIONAL = "optional";
        public static final String PROPERTY_VALIDATION = "validation";

        private String name;
        private boolean optional;
        private TemplateVariableValidation validation;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isOptional() {
            return optional;
        }

        public void setOptional(boolean optional) {
            this.optional = optional;
        }

        public TemplateVariableValidation getValidation() {
            return validation;
        }

        public void setValidation(TemplateVariableValidation validation) {
            this.validation = validation;
        }

    }

    public static class TemplateVariableValidation {

        public static final String PROPERTY_MIN_LENGTH = "minLength";
        public static final String PROPERTY_MAX_LENGTH = "maxLength";
        public static final String PROPERTY_REGULAR_EXPRESSION = "regularExpression";

        private Integer minLength;
        private Integer maxLength;
        private String regularExpression;

        public Integer getMinLength() {
            return minLength;
        }

        public void setMinLength(Integer minLength) {
            this.minLength = minLength;
        }

        public Integer getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(Integer maxLength) {
            this.maxLength = maxLength;
        }

        public String getRegularExpression() {
            return regularExpression;
        }

        public void setRegularExpression(String regularExpression) {
            this.regularExpression = regularExpression;
        }
    }

    @Override
    public Class<TemplateDefinition> getJSONTargetClass() {
        return TemplateDefinition.class;
    }

}
