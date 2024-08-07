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
 * OpenApiExecutionProfileUpdateConfigurationsInner
 */
@JsonPropertyOrder({ OpenApiExecutionProfileUpdateConfigurationsInner.JSON_PROPERTY_UUID })

public class OpenApiExecutionProfileUpdateConfigurationsInner implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String JSON_PROPERTY_UUID = "uuid";
    private String uuid;

    public OpenApiExecutionProfileUpdateConfigurationsInner() {
    }

    public OpenApiExecutionProfileUpdateConfigurationsInner uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * Add uuid for configuration to use here
     *
     * @return uuid
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_UUID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUuid() {
        return uuid;
    }

    @JsonProperty(JSON_PROPERTY_UUID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Return true if this ExecutionProfileUpdate_configurations_inner object is
     * equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenApiExecutionProfileUpdateConfigurationsInner executionProfileUpdateConfigurationsInner = (OpenApiExecutionProfileUpdateConfigurationsInner) o;
        return Objects.equals(uuid, executionProfileUpdateConfigurationsInner.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OpenApiExecutionProfileUpdateConfigurationsInner {\n");
        sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
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

        // add `uuid` to the URL query string
        if (getUuid() != null) {
            joiner.add(String.format("%suuid%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getUuid()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        return joiner.toString();
    }
}
