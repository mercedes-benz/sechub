package com.mercedesbenz.sechub.sereco.metadata;

import java.util.Objects;

public class SerecoWebAttack {

    String vector;
    SerecoWebEvidence evidence;

    public String getVector() {
        return vector;
    }

    public void setVector(String vector) {
        this.vector = vector;
    }

    /**
     *
     * @return evidence or <code>null</code> when not defined
     */
    public SerecoWebEvidence getEvidence() {
        return evidence;
    }

    public void setEvidence(SerecoWebEvidence evicence) {
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
        SerecoWebAttack other = (SerecoWebAttack) obj;
        return Objects.equals(evidence, other.evidence) && Objects.equals(vector, other.vector);
    }

    @Override
    public String toString() {
        return "SerecoWebAttack [" + (vector != null ? "vector=" + vector + ", " : "") + (evidence != null ? "evicence=" + evidence : "") + "]";
    }

}