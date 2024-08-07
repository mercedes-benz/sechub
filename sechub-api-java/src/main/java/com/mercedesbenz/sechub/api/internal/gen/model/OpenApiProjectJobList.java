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
import java.math.BigDecimal;
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
 * OpenApiProjectJobList
 */
@JsonPropertyOrder({ OpenApiProjectJobList.JSON_PROPERTY_TOTAL_PAGES, OpenApiProjectJobList.JSON_PROPERTY_PAGE, OpenApiProjectJobList.JSON_PROPERTY_CONTENT })

public class OpenApiProjectJobList implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String JSON_PROPERTY_TOTAL_PAGES = "totalPages";
    private BigDecimal totalPages;

    public static final String JSON_PROPERTY_PAGE = "page";
    private BigDecimal page;

    public static final String JSON_PROPERTY_CONTENT = "content";
    private List<OpenApiProjectJobListContentInner> content;

    public OpenApiProjectJobList() {
    }

    public OpenApiProjectJobList totalPages(BigDecimal totalPages) {
        this.totalPages = totalPages;
        return this;
    }

    /**
     * The total pages available
     *
     * @return totalPages
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_TOTAL_PAGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public BigDecimal getTotalPages() {
        return totalPages;
    }

    @JsonProperty(JSON_PROPERTY_TOTAL_PAGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTotalPages(BigDecimal totalPages) {
        this.totalPages = totalPages;
    }

    public OpenApiProjectJobList page(BigDecimal page) {
        this.page = page;
        return this;
    }

    /**
     * The page number
     *
     * @return page
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_PAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public BigDecimal getPage() {
        return page;
    }

    @JsonProperty(JSON_PROPERTY_PAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPage(BigDecimal page) {
        this.page = page;
    }

    public OpenApiProjectJobList content(List<OpenApiProjectJobListContentInner> content) {
        this.content = content;
        return this;
    }

    public OpenApiProjectJobList addContentItem(OpenApiProjectJobListContentInner contentItem) {
        if (content == null) {
            content = new ArrayList<>();
        }
        content.add(contentItem);
        return this;
    }

    /**
     * Get content
     *
     * @return content
     **/
    @javax.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_CONTENT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<OpenApiProjectJobListContentInner> getContent() {
        return content;
    }

    @JsonProperty(JSON_PROPERTY_CONTENT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContent(List<OpenApiProjectJobListContentInner> content) {
        this.content = content;
    }

    /**
     * Return true if this ProjectJobList object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenApiProjectJobList projectJobList = (OpenApiProjectJobList) o;
        return Objects.equals(totalPages, projectJobList.totalPages) && Objects.equals(page, projectJobList.page)
                && Objects.equals(content, projectJobList.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalPages, page, content);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OpenApiProjectJobList {\n");
        sb.append("    totalPages: ").append(toIndentedString(totalPages)).append("\n");
        sb.append("    page: ").append(toIndentedString(page)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
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

        // add `totalPages` to the URL query string
        if (getTotalPages() != null) {
            joiner.add(String.format("%stotalPages%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getTotalPages()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `page` to the URL query string
        if (getPage() != null) {
            joiner.add(String.format("%spage%s=%s", prefix, suffix,
                    URLEncoder.encode(String.valueOf(getPage()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
        }

        // add `content` to the URL query string
        if (getContent() != null) {
            for (int i = 0; i < getContent().size(); i++) {
                if (getContent().get(i) != null) {
                    joiner.add(getContent().get(i).toUrlQueryString(String.format("%scontent%s%s", prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }
}
