// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sharedkernel.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.sharedkernel.MustBeDocumented;
import com.mercedesbenz.sechub.storage.sharevolume.spring.AbstractSharedVolumePropertiesSetup;

@Component
public class SharedVolumePropertiesSetup extends AbstractSharedVolumePropertiesSetup {

    /**
     * Folder location for storing files. When using "temp" a temporary folder on
     * server side will be used
     */
    @MustBeDocumented(value = "Defines the root path for shared volume uploads - e.g. for sourcecode.zip etc. When using keyword *temp* as path, this will create a temporary directory (for testing).", scope = "storage")
    @Value("${sechub.storage.sharedvolume.upload.dir:" + UNDEFINED + "}") // we use undefined here. Will be used in #isValid()
    private String configuredUploadDir = UNDEFINED;;

    @Override
    protected String getConfiguredUploadDirectory() {
        return configuredUploadDir;
    }

}