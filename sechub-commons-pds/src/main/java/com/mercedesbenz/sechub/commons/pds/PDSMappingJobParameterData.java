package com.mercedesbenz.sechub.commons.pds;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mercedesbenz.sechub.commons.mapping.NamePatternToIdEntry;

@JsonIgnoreProperties(ignoreUnknown = true) // we do ignore to avoid problems from wrong configured values!
public class PDSMappingJobParameterData {
    private String mappingId;

    private List<NamePatternToIdEntry> entries = new ArrayList<>();

    public List<NamePatternToIdEntry> getEntries() {
        return entries;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }

    public String getMappingId() {
        return mappingId;
    }
}
