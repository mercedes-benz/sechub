package com.mercedesbenz.sechub.adapter.pds;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import com.mercedesbenz.sechub.adapter.AdapterConfigBuilder;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;

public interface PDSAdapterConfigBuilder extends AdapterConfigBuilder {

    PDSAdapterConfigBuilder setBinariesTarFileInputStream(InputStream binariesTarFileInputStream);

    PDSAdapterConfigBuilder setSourceCodeZipFileInputStream(InputStream sourceCodeZipFileInputStream);

    PDSAdapterConfigBuilder setSecHubJobUUID(UUID sechubJobUUID);

    PDSAdapterConfigBuilder setSecHubConfigModel(SecHubConfigurationModel model);

    PDSAdapterConfigBuilder setSourceZipFileChecksum(String sourceZipFileChecksum);

    PDSAdapterConfigBuilder setPDSProductIdentifier(String productIdentifier);

    /**
     * Set job parameters - mandatory
     *
     * @param jobParameters a map with key values
     * @return builder
     */
    PDSAdapterConfigBuilder setJobParameters(Map<String, String> jobParameters);

}