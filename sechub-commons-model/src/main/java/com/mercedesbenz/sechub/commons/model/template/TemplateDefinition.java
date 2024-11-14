// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.model.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.mercedesbenz.sechub.commons.model.JSONable;

@JsonPropertyOrder({ "id", "type", "variables", "assets" })
public class TemplateDefinition implements JSONable<TemplateDefinition> {

    private static TemplateDefinition IMPORTER = new TemplateDefinition();

    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_ASSET_ID = "assetId";
    public static final String PROPERTY_VARIABLES = "variables";

    private TemplateType type;

    private String assetId;
    private List<TemplateVariable> variables = new ArrayList<>();

    private String id;

    public TemplateDefinition() {
    }

    public static TemplateDefinitionBuilder builder() {
        return new TemplateDefinitionBuilder();
    }

    public static TemplateDefinition from(String json) {
        return IMPORTER.fromJSON(json);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getAssetId() {
        return assetId;
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

    @Override
    public Class<TemplateDefinition> getJSONTargetClass() {
        return TemplateDefinition.class;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, id, type, variables);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TemplateDefinition other = (TemplateDefinition) obj;
        return Objects.equals(assetId, other.assetId) && Objects.equals(id, other.id) && type == other.type && Objects.equals(variables, other.variables);
    }

    public static class TemplateDefinitionBuilder {

        private String assetId;
        private String templateId;
        private TemplateType templateType;

        private TemplateDefinitionBuilder() {

        }

        public TemplateDefinitionBuilder assetId(String assetId) {
            this.assetId = assetId;
            return this;
        }

        public TemplateDefinitionBuilder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        public TemplateDefinitionBuilder templateType(TemplateType templateType) {
            this.templateType = templateType;
            return this;
        }

        public TemplateDefinition build() {
            if (assetId == null) {
                throw new IllegalStateException("assetId not defined");
            }
            if (templateId == null) {
                throw new IllegalStateException("templateId not defined");
            }
            if (templateType == null) {
                throw new IllegalStateException("templateType not defined");
            }
            TemplateDefinition def = new TemplateDefinition();
            def.id = templateId;
            def.type = templateType;
            def.assetId = assetId;

            return def;
        }

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

        @Override
        public int hashCode() {
            return Objects.hash(name, optional, validation);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TemplateVariable other = (TemplateVariable) obj;
            return Objects.equals(name, other.name) && optional == other.optional && Objects.equals(validation, other.validation);
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

        @Override
        public int hashCode() {
            return Objects.hash(maxLength, minLength, regularExpression);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TemplateVariableValidation other = (TemplateVariableValidation) obj;
            return Objects.equals(maxLength, other.maxLength) && Objects.equals(minLength, other.minLength)
                    && Objects.equals(regularExpression, other.regularExpression);
        }
    }

}
