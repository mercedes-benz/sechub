package com.daimler.sechub.sarif.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Properties {
    private Set<String> tags;

    public Properties() {
        this.tags = new LinkedHashSet<String>();
    }

    public Properties(Set<String> tags) {
        this.tags = tags;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "Properties [tags=" + tags + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Properties)) {
            return false;
        }
        Properties other = (Properties) obj;
        return Objects.equals(tags, other.tags);
    }
}
