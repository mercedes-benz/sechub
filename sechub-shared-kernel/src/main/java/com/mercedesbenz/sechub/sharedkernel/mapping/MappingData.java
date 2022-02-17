// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.mapping;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.model.JSONable;
import com.mercedesbenz.sechub.sharedkernel.MustBeKeptStable;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
@MustBeKeptStable("This class is used for updating/fetchin mapping data over REST API")
public class MappingData implements JSONable<MappingData> {

    public final static String PROPERTY_ENTRIES = "entries";

    private static MappingData INSTANCE = new MappingData();

    List<MappingEntry> entries = new ArrayList<>();

    public List<MappingEntry> getEntries() {
        return entries;
    }

    @Override
    public Class<MappingData> getJSONTargetClass() {
        return MappingData.class;
    }

    public static MappingData fromString(String json) {
        return INSTANCE.fromJSON(json);
    }

}
