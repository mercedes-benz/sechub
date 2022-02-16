package com.mercedesbenz.sechub.commons.model.web;

import java.util.Objects;

public class SecHubReportWebAttack {

    private String vector;
    private SecHubReportWebEvidence evidence;

    public String getVector() {
        return vector;
    }

    public void setVector(String vector) {
        this.vector = vector;
    }

    public SecHubReportWebEvidence getEvidence() {
        return evidence;
    }

    public void setEvidence(SecHubReportWebEvidence evicence) {
        this.evidence = evicence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(evidence, vector);
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
        SecHubReportWebAttack other = (SecHubReportWebAttack) obj;
        return Objects.equals(evidence, other.evidence) && Objects.equals(vector, other.vector);
    }

    @Override
    public String toString() {
        return "SecHubReportWebAttack [" + (vector != null ? "vector=" + vector + ", " : "") + (evidence != null ? "evicence=" + evidence : "") + "]";
    }

}