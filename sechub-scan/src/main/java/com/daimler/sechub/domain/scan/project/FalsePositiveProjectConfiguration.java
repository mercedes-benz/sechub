package com.daimler.sechub.domain.scan.project;

import java.util.ArrayList;
import java.util.List;

import com.daimler.sechub.sharedkernel.MustBeKeptStable;
import com.daimler.sechub.sharedkernel.util.JSONable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@JsonInclude(value = Include.NON_ABSENT)
@MustBeKeptStable
public class FalsePositiveProjectConfiguration implements JSONable<FalsePositiveProjectConfiguration>{
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
