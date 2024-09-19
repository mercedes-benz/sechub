// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.encryption.InitializationVector;
import com.mercedesbenz.sechub.pds.encryption.PDSEncryptionException;
import com.mercedesbenz.sechub.pds.encryption.PDSEncryptionService;

@Component
public class PDSJobConfigurationAccess {

    @Autowired
    PDSEncryptionService encryptionService;

    public PDSJobConfiguration resolveUnEncryptedJobConfiguration(PDSJob job) throws PDSEncryptionException {
        if (job == null) {
            throw new IllegalArgumentException("job parameter may not be null!");
        }
        byte[] encryptedConfiguration = job.getEncryptedConfiguration();
        byte[] encryptionInitialVectorData = job.getEncryptionInitialVectorData();

        try {
            if (encryptedConfiguration == null) {
                throw new IllegalStateException("No encrypted configuration found for PDS job: " + job.getUUID());
            }
            if (encryptionInitialVectorData == null) {
                throw new IllegalStateException("No initial vector data found for PDS job: " + job.getUUID());
            }

            String json = encryptionService.decryptString(encryptedConfiguration, new InitializationVector(encryptionInitialVectorData));
            PDSJobConfiguration configuration = PDSJobConfiguration.fromJSON(json);
            return configuration;

        } catch (Exception e) {
            throw new PDSEncryptionException("""
                    Was not able to decrypt job configuration for PDS job: %s
                    Suggestion: start a new PDS job with same configuration.
                    """.formatted(job.getUUID()), e);
        }
    }

}
