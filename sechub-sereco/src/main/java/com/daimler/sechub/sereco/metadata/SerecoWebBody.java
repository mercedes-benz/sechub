package com.daimler.sechub.sereco.metadata;

import java.util.Objects;

public class SerecoWebBody {

    String text;
    String binary;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getBinary() {
        return binary;
    }

    public void setBinary(String binary) {
        this.binary = binary;
    }

    @Override
    public int hashCode() {
        return Objects.hash(binary, text);
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
        SerecoWebBody other = (SerecoWebBody) obj;
        return Objects.equals(binary, other.binary) && Objects.equals(text, other.text);
    }

    @Override
    public String toString() {
        return "SerecoWebBody [" + (text != null ? "\ntext=" + text + ", " : "") + (binary != null ? "\nbinary=" + binary : "") + "]";
    }

}