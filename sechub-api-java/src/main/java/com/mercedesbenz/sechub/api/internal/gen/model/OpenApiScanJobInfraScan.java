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
 * Infrastructure configuration block
 */
@JsonPropertyOrder({ OpenApiScanJobInfraScan.JSON_PROPERTY_URIS, OpenApiScanJobInfraScan.JSON_PROPERTY_IPS })

public class OpenApiScanJobInfraScan implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String JSON_PROPERTY_URIS = "uris";
    private List<String> uris;

    public static final String JSON_PROPERTY_IPS = "ips";
    private List<String> ips;

    public OpenApiScanJobInfraScan() {
    }

    public OpenApiScanJobInfraScan uris(List<String> uris) {
        this.uris = uris;
        return this;
    }

    public OpenApiScanJobInfraScan addUrisItem(String urisItem) {
        if (uris == null) {
            uris = new ArrayList<>();
        }
        uris.add(urisItem);
        return this;
    }

    /**
     * Infrastructure URIs to scan for
     *
     * @return uris
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_URIS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getUris() {
        return uris;
    }

    @JsonProperty(JSON_PROPERTY_URIS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUris(List<String> uris) {
        this.uris = uris;
    }

    public OpenApiScanJobInfraScan ips(List<String> ips) {
        this.ips = ips;
        return this;
    }

    public OpenApiScanJobInfraScan addIpsItem(String ipsItem) {
        if (ips == null) {
            ips = new ArrayList<>();
        }
        ips.add(ipsItem);
        return this;
    }

    /**
     * Infrastructure IPs to scan for
     *
     * @return ips
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_IPS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getIps() {
        return ips;
    }

    @JsonProperty(JSON_PROPERTY_IPS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIps(List<String> ips) {
        this.ips = ips;
    }

    /**
     * Return true if this ScanJob_infraScan object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenApiScanJobInfraScan scanJobInfraScan = (OpenApiScanJobInfraScan) o;
        return Objects.equals(uris, scanJobInfraScan.uris) && Objects.equals(ips, scanJobInfraScan.ips);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uris, ips);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OpenApiScanJobInfraScan {\n");
        sb.append("    uris: ").append(toIndentedString(uris)).append("\n");
        sb.append("    ips: ").append(toIndentedString(ips)).append("\n");
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

        // add `uris` to the URL query string
        if (getUris() != null) {
            for (int i = 0; i < getUris().size(); i++) {
                joiner.add(String.format("%suris%s%s=%s", prefix, suffix, "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        URLEncoder.encode(String.valueOf(getUris().get(i)), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
            }
        }

        // add `ips` to the URL query string
        if (getIps() != null) {
            for (int i = 0; i < getIps().size(); i++) {
                joiner.add(String.format("%sips%s%s=%s", prefix, suffix, "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        URLEncoder.encode(String.valueOf(getIps().get(i)), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
            }
        }

        return joiner.toString();
    }
}
