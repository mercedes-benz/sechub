package com.daimler.sechub.sereco.metadata;

import java.util.Objects;

public class SerecoAttack {
    String vector;
    SerecoEvidence evicence;

    public String getVector() {
        return vector;
    }

    public void setVector(String vector) {
        this.vector = vector;
    }

    public SerecoEvidence getEvicence() {
        return evicence;
    }

    public void setEvicence(SerecoEvidence evicence) {
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
        SerecoAttack other = (SerecoAttack) obj;
        return Objects.equals(evicence, other.evicence) && Objects.equals(vector, other.vector);
    }

}