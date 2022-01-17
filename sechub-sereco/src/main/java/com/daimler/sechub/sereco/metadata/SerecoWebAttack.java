package com.daimler.sechub.sereco.metadata;

import java.util.Objects;

public class SerecoWebAttack {
    
    String vector;
    SerecoWebEvidence evicence;

    public String getVector() {
        return vector;
    }

    public void setVector(String vector) {
        this.vector = vector;
    }

    public SerecoWebEvidence getEvicence() {
        return evicence;
    }

    public void setEvicence(SerecoWebEvidence evicence) {
        this.evicence = evicence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(evicence, vector);
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
        return Objects.equals(evicence, other.evicence) && Objects.equals(vector, other.vector);
    }

    @Override
    public String toString() {
        return "SerecoWebAttack [" + (vector != null ? "vector=" + vector + ", " : "") + (evicence != null ? "evicence=" + evicence : "") + "]";
    }

}