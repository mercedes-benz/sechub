// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;

public interface PDSAdapterConfigurator {

    void setPdsProductIdentifier(String pdsProductIdentifier);

    void setSourceCodeZipFileInputStreamOrNull(InputStream sourceCodeZipFileInputStreamOrNull);

    void setSourceCodeZipFileChecksumOrNull(String sourceCodeZipFileChecksumOrNull);

    void setSourceCodeZipFileSizeInBytes(long sourceCodeZipFileSizeInBytes);

    void setBinaryTarFileInputStreamOrNull(InputStream binaryTarFileInputStreamOrNull);

    void setBinariesTarFileChecksumOrNull(String binaryTarFileChecksum);

    void setBinariesTarFileSizeInBytes(long binariesTarFileSizeInBytes);

    void setSecHubJobUUID(UUID secHubJobUUID);

    void setJobParameters(Map<String, String> jobParameters);

    void setSecHubConfigurationModel(SecHubConfigurationModel secHubConfigurationModel);

    void setReusingSecHubStorage(boolean reusingSecHubStorage);

    void setSourceCodeZipFileRequired(boolean sourceCodeZipFileRequired);

    void setBinaryTarFileRequired(boolean binaryTarFileRequired);

    void setScanType(ScanType scanType);

    /**
     * Will be automatically called by {@link #configure()}. and validates defined
     * parts only
     */
    void validateNonCalculatedParts();

    /**
     * Will be automatically called by {@link #configure()}. If the calculated parts
     * needs a validation as well, this must be done also here. The
     * {@link #validateNonCalculatedParts()} method does only check the non
     * calculated parts.
     */
    void calculate();

    /**
     * First {@link #validateNonCalculatedParts()} will be called by this method.
     * Then the {@link #calculate()} method is called. The default implementation
     * does this already and should not be changed.
     */
    public default void configure() {
        validateNonCalculatedParts();
        calculate();
    }

    void setPDSScriptTrustsAllCertificates(boolean trustAllCertificates);

    void setResilienceMaxRetries(int maxRetries);

    void setResilienceTimeToWaitBeforeRetryInMilliseconds(long milliseconds);

}