// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.upload;

import static com.mercedesbenz.sechub.pds.commons.core.config.PDSStorageConstants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.storage.sharevolume.spring.AbstractSharedVolumePropertiesSetup;

@Component
public class PrepareWrapperSharedVolumePropertiesSetup extends AbstractSharedVolumePropertiesSetup {

    /**
     * Folder location for storing files. When using "temp" a temporary folder on
     * server side will be used
     */
    @Value("${" + PDS_STORAGE_SHAREDVOLUME_UPLOAD_DIR + ":" + UNDEFINED_UPLOAD_DIR + "}") // we use undefined here. Will be used in #isValid()
    private String configuredUploadDir;

    @Override
    protected String getConfiguredUploadDirectory() {
        return configuredUploadDir;
    }

}