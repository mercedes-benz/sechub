// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.project;

import java.util.Objects;

public class FalsePositiveCodeMetaData {
    
    public static final String PROPERTY_START = "start";
    public static final String PROPERTY_END = "end";

    private FalsePositiveCodePartMetaData start;
    private FalsePositiveCodePartMetaData end;

    public FalsePositiveCodePartMetaData getStart() {
        return start;
    }

    public void setStart(FalsePositiveCodePartMetaData start) {
        this.start = start;
    }

    public FalsePositiveCodePartMetaData getEnd() {
        return end;
    }

    public void setEnd(FalsePositiveCodePartMetaData end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "FalsePositiveCodeMetaData [start=" + start + ", end=" + end + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(end, start);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FalsePositiveCodeMetaData other = (FalsePositiveCodeMetaData) obj;
        return Objects.equals(end, other.end) && Objects.equals(start, other.start);
    }

}
