// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import java.util.Objects;

public class FalsePositiveWebResponseMetaData {

    public static final String PROPERTY_STATUSCODE = "statusCode";
    public static final String PROPERTY_EVIDENCE = "evidence";

    private String evidence;
    private int statusCode;

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return "FalsePositiveWebResponseMetaData [" + (evidence != null ? "evidence=" + evidence + ", " : "") + "statusCode=" + statusCode + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(evidence, statusCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FalsePositiveWebResponseMetaData other = (FalsePositiveWebResponseMetaData) obj;
        return Objects.equals(evidence, other.evidence) && statusCode == other.statusCode;
    }

}
