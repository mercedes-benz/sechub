// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.storage.sharevolume.spring;

import static java.util.Objects.*;

import java.nio.file.Path;
import java.util.UUID;

import com.mercedesbenz.sechub.storage.core.JobStorage;

public class SharedVolumeJobStorage extends AbstractSharedVolumeStorage implements JobStorage {

    public SharedVolumeJobStorage(Path rootLocation, String storagePath, UUID jobUUID) {
        super(rootLocation, storagePath, jobUUID);
        requireNonNull(jobUUID, "jobUUID may not be null");
    }

}
