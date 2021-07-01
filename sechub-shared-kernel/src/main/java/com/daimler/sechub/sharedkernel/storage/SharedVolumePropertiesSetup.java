// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.daimler.sechub.sharedkernel.MustBeDocumented;
import com.daimler.sechub.storage.sharevolume.spring.AbstractSharedVolumePropertiesSetup;

@Component
public class SharedVolumePropertiesSetup extends AbstractSharedVolumePropertiesSetup {

    /**
     * Folder location for storing files. When using "temp" a temporary folder on server side will be used
     */
	@MustBeDocumented(value="Defines the root path for shared volume uploads - e.g. for sourcecode.zip etc. When using keyword *temp* as path, this will create a temporary directory (for testing).",scope="storage")
	@Value("${sechub.storage.sharedvolume.upload.dir:"+UNDEFINED_UPLOAD_DIR+"}") // we use undefined here. Will be used in #isValid()
    private String configuredUploadDir;
	
	@Override
	protected String getConfiguredUploadDirectory() {
	    return configuredUploadDir;
	}

}