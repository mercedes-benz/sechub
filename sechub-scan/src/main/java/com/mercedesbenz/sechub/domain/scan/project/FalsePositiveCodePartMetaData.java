// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import java.util.Objects;

public class FalsePositiveCodePartMetaData {

    public static final String PROPERTY_LOCATION = "location";
    public static final String PROPERTY_RELEVANT_PART = "relevantPart";
    public static final String PROPERTY_SOURCE_CODE = "sourceCode";
    private String location;
    private String relevantPart;
    private String sourceCode;

    public String getLocation() {
        return location;
    }

    public String getRelevantPart() {
        return relevantPart;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setRelevantPart(String relevantPart) {
        this.relevantPart = relevantPart;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    @Override
    public String toString() {
        return "FalsePositiveCodePartMetaData [location=" + location + ", relevantPart=" + relevantPart + ", sourceCode=" + sourceCode + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, relevantPart, sourceCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FalsePositiveCodePartMetaData other = (FalsePositiveCodePartMetaData) obj;
        return Objects.equals(location, other.location) && Objects.equals(relevantPart, other.relevantPart) && Objects.equals(sourceCode, other.sourceCode);
    }

}
