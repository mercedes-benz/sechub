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
 * OpenApiProjectJobListContentInner
 */
@JsonPropertyOrder({ OpenApiProjectJobListContentInner.JSON_PROPERTY_EXECUTED_BY, OpenApiProjectJobListContentInner.JSON_PROPERTY_META_DATA,
        OpenApiProjectJobListContentInner.JSON_PROPERTY_JOB_U_U_I_D, OpenApiProjectJobListContentInner.JSON_PROPERTY_CREATED,
        OpenApiProjectJobListContentInner.JSON_PROPERTY_EXECUTION_RESULT, OpenApiProjectJobListContentInner.JSON_PROPERTY_EXECUTION_STATE,
        OpenApiProjectJobListContentInner.JSON_PROPERTY_ENDED, OpenApiProjectJobListContentInner.JSON_PROPERTY_STARTED,
        OpenApiProjectJobListContentInner.JSON_PROPERTY_TRAFFIC_LIGHT })

public class OpenApiProjectJobListContentInner implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String JSON_PROPERTY_EXECUTED_BY = "executedBy";
    private String executedBy;

    public static final String JSON_PROPERTY_META_DATA = "metaData";
    private OpenApiProjectJobListContentInnerMetaData metaData;

    public static final String JSON_PROPERTY_JOB_U_U_I_D = "jobUUID";
    private String jobUUID;

    public static final String JSON_PROPERTY_CREATED = "created";
    private String created;

    public static final String JSON_PROPERTY_EXECUTION_RESULT = "executionResult";
    private String executionResult;

    public static final String JSON_PROPERTY_EXECUTION_STATE = "executionState";
    private String executionState;

    public static final String JSON_PROPERTY_ENDED = "ended";
    private String ended;

    public static final String JSON_PROPERTY_STARTED = "started";
    private String started;

    public static final String JSON_PROPERTY_TRAFFIC_LIGHT = "trafficLight";
    private String trafficLight;

    public OpenApiProjectJobListContentInner() {
    }

    public OpenApiProjectJobListContentInner executedBy(String executedBy) {
        this.executedBy = executedBy;
        return this;
    }

    /**
     * User who initiated the job
     *
     * @return executedBy
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_EXECUTED_BY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getExecutedBy() {
        return executedBy;
    }

    @JsonProperty(JSON_PROPERTY_EXECUTED_BY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExecutedBy(String executedBy) {
        this.executedBy = executedBy;
    }

    public OpenApiProjectJobListContentInner metaData(OpenApiProjectJobListContentInnerMetaData metaData) {
        this.metaData = metaData;
        return this;
    }

    /**
     * Get metaData
     *
     * @return metaData
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_META_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OpenApiProjectJobListContentInnerMetaData getMetaData() {
        return metaData;
    }

    @JsonProperty(JSON_PROPERTY_META_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetaData(OpenApiProjectJobListContentInnerMetaData metaData) {
        this.metaData = metaData;
    }

    public OpenApiProjectJobListContentInner jobUUID(String jobUUID) {
        this.jobUUID = jobUUID;
        return this;
    }

    /**
     * The job uuid
     *
     * @return jobUUID
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_JOB_U_U_I_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getJobUUID() {
        return jobUUID;
    }

    @JsonProperty(JSON_PROPERTY_JOB_U_U_I_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setJobUUID(String jobUUID) {
        this.jobUUID = jobUUID;
    }

    public OpenApiProjectJobListContentInner created(String created) {
        this.created = created;
        return this;
    }

    /**
     * Creation timestamp of job
     *
     * @return created
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_CREATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCreated() {
        return created;
    }

    @JsonProperty(JSON_PROPERTY_CREATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCreated(String created) {
        this.created = created;
    }

    public OpenApiProjectJobListContentInner executionResult(String executionResult) {
        this.executionResult = executionResult;
        return this;
    }

    /**
     * Execution result of job
     *
     * @return executionResult
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_EXECUTION_RESULT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getExecutionResult() {
        return executionResult;
    }

    @JsonProperty(JSON_PROPERTY_EXECUTION_RESULT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExecutionResult(String executionResult) {
        this.executionResult = executionResult;
    }

    public OpenApiProjectJobListContentInner executionState(String executionState) {
        this.executionState = executionState;
        return this;
    }

    /**
     * Execution state of job
     *
     * @return executionState
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_EXECUTION_STATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getExecutionState() {
        return executionState;
    }

    @JsonProperty(JSON_PROPERTY_EXECUTION_STATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExecutionState(String executionState) {
        this.executionState = executionState;
    }

    public OpenApiProjectJobListContentInner ended(String ended) {
        this.ended = ended;
        return this;
    }

    /**
     * End timestamp of job execution
     *
     * @return ended
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_ENDED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getEnded() {
        return ended;
    }

    @JsonProperty(JSON_PROPERTY_ENDED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnded(String ended) {
        this.ended = ended;
    }

    public OpenApiProjectJobListContentInner started(String started) {
        this.started = started;
        return this;
    }

    /**
     * Start timestamp of job execution
     *
     * @return started
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_STARTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getStarted() {
        return started;
    }

    @JsonProperty(JSON_PROPERTY_STARTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStarted(String started) {
        this.started = started;
    }

    public OpenApiProjectJobListContentInner trafficLight(String trafficLight) {
        this.trafficLight = trafficLight;
        return this;
    }

    /**
     * Trafficlight of job - but only available when job has been done. Possible
     * states are GREEN, YELLOW, RED, OFF
     *
     * @return trafficLight
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_TRAFFIC_LIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getTrafficLight() {
        return trafficLight;
    }

    @JsonProperty(JSON_PROPERTY_TRAFFIC_LIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTrafficLight(String trafficLight) {
        this.trafficLight = trafficLight;
    }

    /**
     * Return true if this ProjectJobList_content_inner object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenApiProjectJobListContentInner projectJobListContentInner = (OpenApiProjectJobListContentInner) o;
        return Objects.equals(executedBy, projectJobListContentInner.executedBy) && Objects.equals(metaData, projectJobListContentInner.metaData)
                && Objects.equals(jobUUID, projectJobListContentInner.jobUUID) && Objects.equals(created, projectJobListContentInner.created)
                && Objects.equals(executionResult, projectJobListContentInner.executionResult)
                && Objects.equals(executionState, projectJobListContentInner.executionState) && Objects.equals(ended, projectJobListContentInner.ended)
                && Objects.equals(started, projectJobListContentInner.started) && Objects.equals(trafficLight, projectJobListContentInner.trafficLight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executedBy, metaData, jobUUID, created, executionResult, executionState, ended, started, trafficLight);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OpenApiProjectJobListContentInner {\n");
        sb.append("    executedBy: ").append(toIndentedString(executedBy)).append("\n");
        sb.append("    metaData: ").append(toIndentedString(metaData)).append("\n");
        sb.append("    jobUUID: ").append(toIndentedString(jobUUID)).append("\n");
        sb.append("    created: ").append(toIndentedString(created)).append("\n");
        sb.append("    executionResult: ").append(toIndentedString(executionResult)).append("\n");
        sb.append("    executionState: ").append(toIndentedString(executionState)).append("\n");
        sb.append("    ended: ").append(toIndentedString(ended)).append("\n");
        sb.append("    started: ").append(toIndentedString(started)).append("\n");
        sb.append("    trafficLight: ").append(toIndentedString(trafficLight)).append("\n");
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

        // add `executedBy` to the URL query string
        if (getExecutedBy() != null) {
            joiner.add(String.format("%sexecutedBy%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getExecutedBy()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `metaData` to the URL query string
        if (getMetaData() != null) {
            joiner.add(getMetaData().toUrlQueryString(prefix + "metaData" + suffix));
        }

        // add `jobUUID` to the URL query string
        if (getJobUUID() != null) {
            joiner.add(String.format("%sjobUUID%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getJobUUID()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `created` to the URL query string
        if (getCreated() != null) {
            joiner.add(String.format("%screated%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getCreated()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `executionResult` to the URL query string
        if (getExecutionResult() != null) {
            joiner.add(String.format("%sexecutionResult%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getExecutionResult()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `executionState` to the URL query string
        if (getExecutionState() != null) {
            joiner.add(String.format("%sexecutionState%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getExecutionState()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `ended` to the URL query string
        if (getEnded() != null) {
            joiner.add(String.format("%sended%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getEnded()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `started` to the URL query string
        if (getStarted() != null) {
            joiner.add(String.format("%sstarted%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getStarted()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `trafficLight` to the URL query string
        if (getTrafficLight() != null) {
            joiner.add(String.format("%strafficLight%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getTrafficLight()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        return joiner.toString();
    }
}
