// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.mapping;

import java.util.Objects;

import com.daimler.sechub.commons.model.JSONable;
import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This class is used for updating/fetchin mapping data over REST API")
public class MappingEntry implements JSONable<MappingEntry> {

    public static final String PROPERTY_PATTERN="pattern";
    public static final String PROPERTY_REPLACEMENT="replacement";
    public static final String PROPERTY_COMMENT="comment";
    
    String pattern;

    String replacement;

    String comment;

    public MappingEntry() {
        /* json */
    }

    public MappingEntry(String pattern, String replacement, String comment) {
        this.pattern = pattern;
        this.replacement = replacement;
        this.comment = comment;
    }

    public String getPattern() {
        return pattern;
    }

    public String getReplacement() {
        return replacement;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public Class<MappingEntry> getJSONTargetClass() {
        return MappingEntry.class;
    }

    @Override
    public int hashCode() {
        return Objects.hash(comment, pattern, replacement);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MappingEntry other = (MappingEntry) obj;
        return Objects.equals(comment, other.comment) && Objects.equals(pattern, other.pattern) && Objects.equals(replacement, other.replacement);
    }

}
