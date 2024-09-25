// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.integrationtest.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubDomainEncryptionStatus;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionStatus;

public class AssertEncryptionStatus {

    private SecHubEncryptionStatus status;

    private static final Logger LOG = LoggerFactory.getLogger(AssertEncryptionStatus.class);

    public static AssertEncryptionStatus assertEncryptionStatus(SecHubEncryptionStatus status) {
        return new AssertEncryptionStatus(status);
    }

    private AssertEncryptionStatus(SecHubEncryptionStatus status) {
        if (status == null) {
            throw new AssertionError("Encryption status was null!");
        }
        this.status = status;
    }

    public AssertDomainEncryptionStatus domain(String domainName) {

        List<SecHubDomainEncryptionStatus> domains = status.getDomains();
        for (SecHubDomainEncryptionStatus domain : domains) {
            if (domain.getName().equalsIgnoreCase(domainName)) {
                return new AssertDomainEncryptionStatus(domain);
            }
        }
        throw new AssertionError("Encryption status was null!");
    }

    public class AssertDomainEncryptionStatus {

        private SecHubDomainEncryptionStatus domain;

        private AssertDomainEncryptionStatus(SecHubDomainEncryptionStatus domain) {
            this.domain = domain;
        }

        /**
         * Return to parent assertion level
         *
         * @return encryption status assert object
         */
        public AssertEncryptionStatus encryptionStatus() {
            return AssertEncryptionStatus.this;
        }

        public AssertDomainEncryptionStatus hasData() {
            if (domain.getData().isEmpty()) {
                throw new AssertionError("No data available for domain: " + domain.getName());
            }
            return this;
        }

        public AssertDomainEncryptionStatus hasData(int expectedAmountOfData) {
            int amount = domain.getData().size();

            if (amount != expectedAmountOfData) {

                dump();

                throw new AssertionError("Not expected amount of data available for domain: " + domain.getName() + ", expected was: " + expectedAmountOfData
                        + ", found: " + amount);
            }
            return this;
        }

        public int getDataSize() {
            return domain.getData().size();
        }

    }

    public AssertEncryptionStatus dump() {
        LOG.info("Dump encrpytion status object:\n{}", status.toFormattedJSON());
        return this;
    }

}
