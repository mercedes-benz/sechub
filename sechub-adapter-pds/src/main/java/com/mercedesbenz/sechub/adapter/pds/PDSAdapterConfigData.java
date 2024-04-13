// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;

public interface PDSAdapterConfigData {

    /**
     * @return an unmodifiable map with job parameters
     */
    Map<String, String> getJobParameters();

    UUID getSecHubJobUUID();

    String getPdsProductIdentifier();

    InputStream getSourceCodeZipFileInputStreamOrNull();

    String getSourceCodeZipFileChecksumOrNull();

    InputStream getBinaryTarFileInputStreamOrNull();

    String getBinariesTarFileChecksumOrNull();

    boolean isReusingSecHubStorage();

    boolean isSourceCodeZipFileRequired();

    boolean isBinaryTarFileRequired();

    SecHubConfigurationModel getSecHubConfigurationModel();

    ScanType getScanType();

    boolean isPDSScriptTrustingAllCertificates();

    Long getBinariesTarFileSizeInBytesOrNull();

    Long getSourceCodeZipFileSizeInBytesOrNull();

    int getResilienceMaxRetries();

    long getResilienceTimeToWaitBeforeRetryInMilliseconds();
}
