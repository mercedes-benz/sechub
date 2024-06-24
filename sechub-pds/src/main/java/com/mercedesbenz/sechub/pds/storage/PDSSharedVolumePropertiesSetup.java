// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.storage;

import static com.mercedesbenz.sechub.pds.commons.core.config.PDSStorageConstants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.environment.SecureEnvironmentVariableKeyValueRegistry;
import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;
import com.mercedesbenz.sechub.storage.sharevolume.spring.AbstractSharedVolumePropertiesSetup;

@Component
public class PDSSharedVolumePropertiesSetup extends AbstractSharedVolumePropertiesSetup {

    /**
     * Folder location for storing files. When using "temp" a temporary folder on
     * server side will be used
     */
    @PDSMustBeDocumented(value = "Defines the root path for shared volume uploads - e.g. for sourcecode.zip etc. When using keyword *temp* as path, this will create a temporary directory (for testing).", scope = "storage", secret = true)
    @Value("${" + PDS_STORAGE_SHAREDVOLUME_UPLOAD_DIR + ":" + UNDEFINED + "}") // we use undefined here. Will be used in #isValid()
    private String configuredUploadDir;

    @Override
    protected String getConfiguredUploadDirectory() {
        return configuredUploadDir;
    }

    public void registerOnlyAllowedAsEnvironmentVariables(SecureEnvironmentVariableKeyValueRegistry registry) {
        if (UNDEFINED.equals(configuredUploadDir)) {
            // not sensitive, ignore
            return;
        }
        registry.register(registry.newEntry().key(PDS_STORAGE_SHAREDVOLUME_UPLOAD_DIR).notNullValue(configuredUploadDir));
    }

}