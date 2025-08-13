package com.mercedesbenz.sechub.plugin.idea.falsepositive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class FalsePositivesList {
    private static final String FALSE_POSITIVES_PROPERTY = "falsePositives";
    private final List<FalsePositive> falsePositives;

    @JsonCreator
    public FalsePositivesList(@JsonProperty(FALSE_POSITIVES_PROPERTY) List<FalsePositive> falsePositives) {
        requireNonNull(falsePositives, "Property 'entries' must not be null");
        this.falsePositives = falsePositives;
    }

    public List<FalsePositive> getFalsePositives() {
        return falsePositives;
    }

    public void addFalsePositive(FalsePositive falsePositive) {
        requireNonNull(falsePositive, "Parameter 'falsePositive' must not be null");
        falsePositives.add(falsePositive);
    }
}
