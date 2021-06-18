package com.daimler.sechub.sarif.model;

import java.util.Objects;

// see https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10128043
public class ReportingConfiguration {

    private Level level;

    public void setLevel(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(level);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReportingConfiguration other = (ReportingConfiguration) obj;
        return level == other.level;
    }
}
