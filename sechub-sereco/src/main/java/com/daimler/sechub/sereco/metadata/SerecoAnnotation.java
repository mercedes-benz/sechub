// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.metadata;

import java.util.Objects;

/**
 * Sereco annotation - represents additional data fetched by importers
 * @author Albert Tregnaghi
 *
 */
public class SerecoAnnotation {
    
    private SerecoAnnotationType type;

    private String value;

    public void setType(SerecoAnnotationType type) {
        this.type = type;
    }

    public SerecoAnnotationType getType() {
        return type;
    }

    public void setValue(String text) {
        this.value = text;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SerecoAnnotation other = (SerecoAnnotation) obj;
        return Objects.equals(value, other.value) && type == other.type;
    }
}
