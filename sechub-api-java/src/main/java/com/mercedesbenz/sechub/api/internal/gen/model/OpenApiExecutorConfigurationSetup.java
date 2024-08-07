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
 * OpenApiExecutorConfigurationSetup
 */
@JsonPropertyOrder({ OpenApiExecutorConfigurationSetup.JSON_PROPERTY_BASE_U_R_L, OpenApiExecutorConfigurationSetup.JSON_PROPERTY_CREDENTIALS,
        OpenApiExecutorConfigurationSetup.JSON_PROPERTY_JOB_PARAMETERS })

public class OpenApiExecutorConfigurationSetup implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String JSON_PROPERTY_BASE_U_R_L = "baseURL";
    private String baseURL;

    public static final String JSON_PROPERTY_CREDENTIALS = "credentials";
    private OpenApiExecutorConfigurationSetupCredentials credentials;

    public static final String JSON_PROPERTY_JOB_PARAMETERS = "jobParameters";
    private List<OpenApiExecutorConfigurationSetupJobParametersInner> jobParameters;

    public OpenApiExecutorConfigurationSetup() {
    }

    public OpenApiExecutorConfigurationSetup baseURL(String baseURL) {
        this.baseURL = baseURL;
        return this;
    }

    /**
     * Base URL to the product
     *
     * @return baseURL
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_BASE_U_R_L)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getBaseURL() {
        return baseURL;
    }

    @JsonProperty(JSON_PROPERTY_BASE_U_R_L)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public OpenApiExecutorConfigurationSetup credentials(OpenApiExecutorConfigurationSetupCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    /**
     * Get credentials
     *
     * @return credentials
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_CREDENTIALS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OpenApiExecutorConfigurationSetupCredentials getCredentials() {
        return credentials;
    }

    @JsonProperty(JSON_PROPERTY_CREDENTIALS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCredentials(OpenApiExecutorConfigurationSetupCredentials credentials) {
        this.credentials = credentials;
    }

    public OpenApiExecutorConfigurationSetup jobParameters(List<OpenApiExecutorConfigurationSetupJobParametersInner> jobParameters) {
        this.jobParameters = jobParameters;
        return this;
    }

    public OpenApiExecutorConfigurationSetup addJobParametersItem(OpenApiExecutorConfigurationSetupJobParametersInner jobParametersItem) {
        if (jobParameters == null) {
            jobParameters = new ArrayList<>();
        }
        jobParameters.add(jobParametersItem);
        return this;
    }

    /**
     * Get jobParameters
     *
     * @return jobParameters
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_JOB_PARAMETERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<OpenApiExecutorConfigurationSetupJobParametersInner> getJobParameters() {
        return jobParameters;
    }

    @JsonProperty(JSON_PROPERTY_JOB_PARAMETERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setJobParameters(List<OpenApiExecutorConfigurationSetupJobParametersInner> jobParameters) {
        this.jobParameters = jobParameters;
    }

    /**
     * Return true if this ExecutorConfiguration_setup object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenApiExecutorConfigurationSetup executorConfigurationSetup = (OpenApiExecutorConfigurationSetup) o;
        return Objects.equals(baseURL, executorConfigurationSetup.baseURL) && Objects.equals(credentials, executorConfigurationSetup.credentials)
                && Objects.equals(jobParameters, executorConfigurationSetup.jobParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseURL, credentials, jobParameters);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OpenApiExecutorConfigurationSetup {\n");
        sb.append("    baseURL: ").append(toIndentedString(baseURL)).append("\n");
        sb.append("    credentials: ").append(toIndentedString(credentials)).append("\n");
        sb.append("    jobParameters: ").append(toIndentedString(jobParameters)).append("\n");
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

        // add `baseURL` to the URL query string
        if (getBaseURL() != null) {
            joiner.add(String.format("%sbaseURL%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getBaseURL()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `credentials` to the URL query string
        if (getCredentials() != null) {
            joiner.add(getCredentials().toUrlQueryString(prefix + "credentials" + suffix));
        }

        // add `jobParameters` to the URL query string
        if (getJobParameters() != null) {
            for (int i = 0; i < getJobParameters().size(); i++) {
                if (getJobParameters().get(i) != null) {
                    joiner.add(getJobParameters().get(i).toUrlQueryString(String.format("%sjobParameters%s%s", prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }
}
