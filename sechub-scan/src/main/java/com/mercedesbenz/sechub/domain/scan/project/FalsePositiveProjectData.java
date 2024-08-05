// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import java.util.Objects;

public class FalsePositiveProjectData {

    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_WEBSCAN = "webScan";
    public static final String PROPERTY_COMMENT = "comment";

    private String id;
    private WebscanFalsePositiveProjectData webScan;
    private String comment;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WebscanFalsePositiveProjectData getWebScan() {
        return webScan;
    }

    public void setWebScan(WebscanFalsePositiveProjectData webScan) {
        this.webScan = webScan;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "FalsePositiveProjectData [id=" + id + ", webScan=" + webScan + ", comment=" + comment + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(comment, id, webScan);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FalsePositiveProjectData other = (FalsePositiveProjectData) obj;
        return Objects.equals(comment, other.comment) && Objects.equals(id, other.id) && Objects.equals(webScan, other.webScan);
    }

}
