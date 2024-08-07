// SPDX-License-Identifier: MIT
/*
 * SecHub API
 * SecHub API description
 *
 * The version of the OpenAPI document: 0.0.0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.mercedesbenz.sechub.api.internal.gen.model;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner
 */
@JsonPropertyOrder({ OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner.JSON_PROPERTY_UNIT,
        OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner.JSON_PROPERTY_DESCRIPTION,
        OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner.JSON_PROPERTY_SELECTOR,
        OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner.JSON_PROPERTY_TYPE,
        OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner.JSON_PROPERTY_VALUE })

public class OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String JSON_PROPERTY_UNIT = "unit";
    private String unit;

    public static final String JSON_PROPERTY_DESCRIPTION = "description";
    private String description;

    public static final String JSON_PROPERTY_SELECTOR = "selector";
    private String selector;

    public static final String JSON_PROPERTY_TYPE = "type";
    private String type;

    public static final String JSON_PROPERTY_VALUE = "value";
    private String value;

    public OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner() {
    }

    public OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner unit(String unit) {
        this.unit = unit;
        return this;
    }

    /**
     * the time unit to wait: millisecond, second, minute, hour, day.
     *
     * @return unit
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_UNIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUnit() {
        return unit;
    }

    @JsonProperty(JSON_PROPERTY_UNIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUnit(String unit) {
        this.unit = unit;
    }

    public OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner description(String description) {
        this.description = description;
        return this;
    }

    /**
     * description
     *
     * @return description
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_DESCRIPTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getDescription() {
        return description;
    }

    @JsonProperty(JSON_PROPERTY_DESCRIPTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDescription(String description) {
        this.description = description;
    }

    public OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner selector(String selector) {
        this.selector = selector;
        return this;
    }

    /**
     * css selector
     *
     * @return selector
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_SELECTOR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSelector() {
        return selector;
    }

    @JsonProperty(JSON_PROPERTY_SELECTOR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSelector(String selector) {
        this.selector = selector;
    }

    public OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner type(String type) {
        this.type = type;
        return this;
    }

    /**
     * action type: username, password, input, click, wait
     *
     * @return type
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(String type) {
        this.type = type;
    }

    public OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner value(String value) {
        this.value = value;
        return this;
    }

    /**
     * value
     *
     * @return value
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getValue() {
        return value;
    }

    @JsonProperty(JSON_PROPERTY_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Return true if this
     * ScanJob_webScan_login_form_script_pages_inner_actions_inner object is equal
     * to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner scanJobWebScanLoginFormScriptPagesInnerActionsInner = (OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner) o;
        return Objects.equals(unit, scanJobWebScanLoginFormScriptPagesInnerActionsInner.unit)
                && Objects.equals(description, scanJobWebScanLoginFormScriptPagesInnerActionsInner.description)
                && Objects.equals(selector, scanJobWebScanLoginFormScriptPagesInnerActionsInner.selector)
                && Objects.equals(type, scanJobWebScanLoginFormScriptPagesInnerActionsInner.type)
                && Objects.equals(value, scanJobWebScanLoginFormScriptPagesInnerActionsInner.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit, description, selector, type, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OpenApiScanJobWebScanLoginFormScriptPagesInnerActionsInner {\n");
        sb.append("    unit: ").append(toIndentedString(unit)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    selector: ").append(toIndentedString(selector)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * Convert the instance into URL query string.
     *
     * @return URL query string
     */
    public String toUrlQueryString() {
        return toUrlQueryString(null);
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        String suffix = "";
        String containerSuffix = "";
        String containerPrefix = "";
        if (prefix == null) {
            // style=form, explode=true, e.g. /pet?name=cat&type=manx
            prefix = "";
        } else {
            // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
            prefix = prefix + "[";
            suffix = "]";
            containerSuffix = "]";
            containerPrefix = "[";
        }

        StringJoiner joiner = new StringJoiner("&");

        // add `unit` to the URL query string
        if (getUnit() != null) {
            joiner.add(String.format("%sunit%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getUnit()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `description` to the URL query string
        if (getDescription() != null) {
            joiner.add(String.format("%sdescription%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getDescription()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `selector` to the URL query string
        if (getSelector() != null) {
            joiner.add(String.format("%sselector%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getSelector()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format("%stype%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getType()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `value` to the URL query string
        if (getValue() != null) {
            joiner.add(String.format("%svalue%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getValue()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        return joiner.toString();
    }
}
