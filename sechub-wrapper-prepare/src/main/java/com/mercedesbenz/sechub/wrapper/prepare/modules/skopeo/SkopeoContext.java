// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import java.io.File;
import java.nio.file.Path;

import com.mercedesbenz.sechub.wrapper.prepare.modules.AbstractPrepareToolContext;

public class SkopeoContext extends AbstractPrepareToolContext {

    private File downloadTarFilename;
    private Path skopeoDownloadDirectory;

    @Override
    public void init(Path workingDirectory) {
        super.init(workingDirectory);

        skopeoDownloadDirectory = workingDirectory.resolve(SkopeoWrapperConstants.DOWNLOAD_DIRECTORY_NAME);
        downloadTarFilename = skopeoDownloadDirectory.resolve(SkopeoWrapperConstants.DOWNLOAD_FILENAME).toFile();
    }

    public File getDownloadTarFile() {
        return downloadTarFilename;
    }

    @Override
    public Path getToolDownloadDirectory() {
        return skopeoDownloadDirectory;
    }

}
