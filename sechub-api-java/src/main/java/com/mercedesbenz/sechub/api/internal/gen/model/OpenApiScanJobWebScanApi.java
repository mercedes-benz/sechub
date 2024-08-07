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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * OpenApiScanJobWebScanApi
 */
@JsonPropertyOrder({ OpenApiScanJobWebScanApi.JSON_PROPERTY_API_DEFINITION_URL, OpenApiScanJobWebScanApi.JSON_PROPERTY_USE,
        OpenApiScanJobWebScanApi.JSON_PROPERTY_TYPE })

public class OpenApiScanJobWebScanApi implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String JSON_PROPERTY_API_DEFINITION_URL = "apiDefinitionUrl";
    private String apiDefinitionUrl;

    public static final String JSON_PROPERTY_USE = "use";
    private List<String> use;

    public static final String JSON_PROPERTY_TYPE = "type";
    private String type;

    public OpenApiScanJobWebScanApi() {
    }

    public OpenApiScanJobWebScanApi apiDefinitionUrl(String apiDefinitionUrl) {
        this.apiDefinitionUrl = apiDefinitionUrl;
        return this;
    }

    /**
     * Specifies an URL to read the API definition from.
     *
     * @return apiDefinitionUrl
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_API_DEFINITION_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getApiDefinitionUrl() {
        return apiDefinitionUrl;
    }

    @JsonProperty(JSON_PROPERTY_API_DEFINITION_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setApiDefinitionUrl(String apiDefinitionUrl) {
        this.apiDefinitionUrl = apiDefinitionUrl;
    }

    public OpenApiScanJobWebScanApi use(List<String> use) {
        this.use = use;
        return this;
    }

    public OpenApiScanJobWebScanApi addUseItem(String useItem) {
        if (use == null) {
            use = new ArrayList<>();
        }
        use.add(useItem);
        return this;
    }

    /**
     * Reference to the data section containing the API definition files. Always use
     * &#39;sources&#39; with &#39;files&#39; instead &#39;folders&#39;.
     *
     * @return use
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_USE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getUse() {
        return use;
    }

    @JsonProperty(JSON_PROPERTY_USE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUse(List<String> use) {
        this.use = use;
    }

    public OpenApiScanJobWebScanApi type(String type) {
        this.type = type;
        return this;
    }

    /**
     * Type of the API definition files that will be provided
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

    /**
     * Return true if this ScanJob_webScan_api object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenApiScanJobWebScanApi scanJobWebScanApi = (OpenApiScanJobWebScanApi) o;
        return Objects.equals(apiDefinitionUrl, scanJobWebScanApi.apiDefinitionUrl) && Objects.equals(use, scanJobWebScanApi.use)
                && Objects.equals(type, scanJobWebScanApi.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiDefinitionUrl, use, type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OpenApiScanJobWebScanApi {\n");
        sb.append("    apiDefinitionUrl: ").append(toIndentedString(apiDefinitionUrl)).append("\n");
        sb.append("    use: ").append(toIndentedString(use)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

        // add `apiDefinitionUrl` to the URL query string
        if (getApiDefinitionUrl() != null) {
            joiner.add(String.format("%sapiDefinitionUrl%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getApiDefinitionUrl()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `use` to the URL query string
        if (getUse() != null) {
            for (int i = 0; i < getUse().size(); i++) {
                joiner.add(String.format("%suse%s%s=%s", prefix, suffix, "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        URLEncoder.encode(String.valueOf(getUse().get(i)), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
            }
        }

        // add `type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format("%stype%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getType()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        return joiner.toString();
    }
}
