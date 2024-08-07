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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * OpenApiMappingConfiguration
 */
@JsonPropertyOrder({ OpenApiMappingConfiguration.JSON_PROPERTY_ENTRIES })

public class OpenApiMappingConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String JSON_PROPERTY_ENTRIES = "entries";
    private List<OpenApiMappingConfigurationEntriesInner> entries;

    public OpenApiMappingConfiguration() {
    }

    public OpenApiMappingConfiguration entries(List<OpenApiMappingConfigurationEntriesInner> entries) {
        this.entries = entries;
        return this;
    }

    public OpenApiMappingConfiguration addEntriesItem(OpenApiMappingConfigurationEntriesInner entriesItem) {
        if (entries == null) {
            entries = new ArrayList<>();
        }
        entries.add(entriesItem);
        return this;
    }

    /**
     * Get entries
     *
     * @return entries
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_ENTRIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<OpenApiMappingConfigurationEntriesInner> getEntries() {
        return entries;
    }

    @JsonProperty(JSON_PROPERTY_ENTRIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEntries(List<OpenApiMappingConfigurationEntriesInner> entries) {
        this.entries = entries;
    }

    /**
     * Return true if this MappingConfiguration object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenApiMappingConfiguration mappingConfiguration = (OpenApiMappingConfiguration) o;
        return Objects.equals(entries, mappingConfiguration.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OpenApiMappingConfiguration {\n");
        sb.append("    entries: ").append(toIndentedString(entries)).append("\n");
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

        // add `entries` to the URL query string
        if (getEntries() != null) {
            for (int i = 0; i < getEntries().size(); i++) {
                if (getEntries().get(i) != null) {
                    joiner.add(getEntries().get(i).toUrlQueryString(String.format("%sentries%s%s", prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }
}
