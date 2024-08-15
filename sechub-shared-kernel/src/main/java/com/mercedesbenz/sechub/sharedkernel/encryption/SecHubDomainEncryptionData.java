package com.mercedesbenz.sechub.sharedkernel.encryption;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

//SPDX-License-Identifier: MIT
public class SecHubDomainEncryptionData {

    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_ALGORITHM = "algorithm";
    public static final String PROPERTY_PASSWORDSOURCE = "passwordSource";
    public static final String PROPERTY_CREATED = "created";
    public static final String PROPERTY_CREATED_FROM = "createdFrom";
    public static final String PROPERTY_USAGE = "usage";

    private String id;

    private SecHubCipherAlgorithm algorithm;

    private SecHubPasswordSource passwordSource = new SecHubPasswordSource();

    private Map<String, Long> usage = new TreeMap<>();

    private String createdFrom;

    private LocalDateTime created;

    public String getId() {
        return id;
    }

    public void setId(String poolId) {
        this.id = poolId;
    }

    public SecHubCipherAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(SecHubCipherAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public SecHubPasswordSource getPasswordSource() {
        return passwordSource;
    }

    public void setPasswordSource(SecHubPasswordSource passwordSource) {
        this.passwordSource = passwordSource;
    }

    public Map<String, Long> getUsage() {
        return usage;
    }

    public void setUsage(Map<String, Long> usage) {
        this.usage = usage;
    }

    public String getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(String createdFrom) {
        this.createdFrom = createdFrom;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

}
