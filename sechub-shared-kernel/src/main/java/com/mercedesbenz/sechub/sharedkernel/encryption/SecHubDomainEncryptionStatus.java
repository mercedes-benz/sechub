package com.mercedesbenz.sechub.sharedkernel.encryption;

import java.util.ArrayList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.JSONable;

public class SecHubDomainEncryptionStatus implements JSONable<SecHubDomainEncryptionStatus> {

    private static final SecHubDomainEncryptionStatus CONVERTER = new SecHubDomainEncryptionStatus();
    private String name;

    private List<SecHubDomainEncryptionData> data = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

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
