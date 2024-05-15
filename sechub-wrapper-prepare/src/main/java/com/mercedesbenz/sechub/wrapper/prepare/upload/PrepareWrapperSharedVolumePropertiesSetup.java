// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;
import com.mercedesbenz.sechub.storage.sharevolume.spring.AbstractSharedVolumePropertiesSetup;

@Component
public class PrepareWrapperSharedVolumePropertiesSetup extends AbstractSharedVolumePropertiesSetup {

    /**
     * Folder location for storing files. When using "temp" a temporary folder on
     * server side will be used
     */
    @PDSMustBeDocumented(value = "Defines the root path for shared volume uploads - e.g. for sourcecode.zip etc. When using keyword *temp* as path, this will create a temporary directory (for testing).", scope = "storage")
    @Value("${pds.storage.sharedvolume.upload.dir:" + UNDEFINED_UPLOAD_DIR + "}") // we use undefined here. Will be used in #isValid()
    private String configuredUploadDir;

    @Override
    protected String getConfiguredUploadDirectory() {
        return configuredUploadDir;
    }

}