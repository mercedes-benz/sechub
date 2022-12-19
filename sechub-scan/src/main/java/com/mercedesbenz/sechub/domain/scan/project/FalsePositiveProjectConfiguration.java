// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.project;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mercedesbenz.sechub.commons.core.MustBeKeptStable;
import com.mercedesbenz.sechub.commons.model.JSONable;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@JsonInclude(value = Include.NON_ABSENT)
@MustBeKeptStable
public class FalsePositiveProjectConfiguration implements JSONable<FalsePositiveProjectConfiguration> {

    public static final String PROPERTY_FALSE_POSITIVES = "falsePositives";

    private static final FalsePositiveProjectConfiguration IMPORT = new FalsePositiveProjectConfiguration();

    private List<FalsePositiveEntry> falsePositives = new ArrayList<>();

    public List<FalsePositiveEntry> getFalsePositives() {
        return falsePositives;
    }

    @Override
    public Class<FalsePositiveProjectConfiguration> getJSONTargetClass() {
        return FalsePositiveProjectConfiguration.class;
    }

    public static final FalsePositiveProjectConfiguration fromJSONString(String json) {
        return IMPORT.fromJSON(json);
    }

}
