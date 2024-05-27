package com.mercedesbenz.sechub.wrapper.prepare.modules.skopeo;

import java.io.File;
import java.nio.file.Path;

import com.mercedesbenz.sechub.wrapper.prepare.modules.ToolContext;

public class SkopeoContext extends ToolContext {

    static final String DOWNLOAD_DIRECTORY_NAME = "skopeo-download";
    private File downloadTarFilename = new File("skopeo-download.tar");

    public void setWorkingDirectory(Path workingDirectory) {
        super.workingDirectory(workingDirectory);
        toolDownloadDirectory = workingDirectory.resolve(DOWNLOAD_DIRECTORY_NAME);
    }

    public void setDownloadTarFilename(File downloadTarFilename) {
        if (downloadTarFilename == null) {
            throw new IllegalArgumentException("Download filename may not be null.");
        }
        if (!downloadTarFilename.getName().endsWith(".tar")) {
            throw new IllegalArgumentException("Download filename must end with .tar.");
        }
        this.downloadTarFilename = downloadTarFilename;
    }

    public File getDownloadTarFilename() {
        return downloadTarFilename;
    }

}
