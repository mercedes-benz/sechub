package com.mercedesbenz.sechub.sharedkernel.encryption;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.JSONable;

public class SecHubDomainEncryptionStatus implements JSONable<SecHubDomainEncryptionStatus> {

    public static final String PROPERTY_DATA = "data";
    public static final String PROPERTY_NAME = "name";

    private static final SecHubDomainEncryptionStatus CONVERTER = new SecHubDomainEncryptionStatus();
    private String name;

    private List<SecHubDomainEncryptionData> data = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the list of encryption data entries. Each entry represents one
     * encryption pool entry!
     *
     * @return list of encryption data entries
     */
    public List<SecHubDomainEncryptionData> getData() {
        return data;
    }

    public static SecHubDomainEncryptionStatus fromString(String json) {
        return CONVERTER.fromJSON(json);
    }

    @Override
    public Class<SecHubDomainEncryptionStatus> getJSONTargetClass() {
        return SecHubDomainEncryptionStatus.class;
    }

}
