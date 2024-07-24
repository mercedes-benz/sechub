package com.mercedesbenz.sechub.sharedkernel.encryption;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

//SPDX-License-Identifier: MIT
public class SecHubDomainEncryptionData {

    private String id;

    private SecHubCipherAlgorithm algorithm;

    private PasswordSourceData passwordSource = new PasswordSourceData();

    private Map<String, Long> usage = new TreeMap<>();

    private String createdFrom;

    private LocalDateTime created;

    public class PasswordSourceData {
        private SecHubCipherPasswordSourceType type;
        private String data;

        public void setData(String data) {
            this.data = data;
        }

        public void setType(SecHubCipherPasswordSourceType type) {
            this.type = type;
        }

        public String getData() {
            return data;
        }

        public SecHubCipherPasswordSourceType getType() {
            return type;
        }
    }

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

    public PasswordSourceData getPasswordSource() {
        return passwordSource;
    }

    public void setPasswordSource(PasswordSourceData passwordSource) {
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
