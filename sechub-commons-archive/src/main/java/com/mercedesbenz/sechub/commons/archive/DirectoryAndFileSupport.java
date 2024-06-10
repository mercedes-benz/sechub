package com.mercedesbenz.sechub.commons.archive;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class DirectoryAndFileSupport {

    public void cleanDirectories(File parentFolder, FileFilter filterAcceptingFilesToDelete) throws IOException {
        if (parentFolder == null) {
            throw new IllegalArgumentException("Parent folder may not be null!");
        }
        cleanDirectoriesRecursive(parentFolder, filterAcceptingFilesToDelete);
    }

    private void cleanDirectoriesRecursive(File parentFolder, FileFilter filter) throws IOException {
        File[] files = parentFolder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if (filter.accept(file)) {
                    /* delete the sub directory recursive */
                    FileUtils.forceDelete(file);
                } else {
                    /* not accepted to delete, but inspect children */
                    cleanDirectories(file, filter);
                }
            } else {
                /* not directory */
                if (filter.accept(file)) {
                    FileUtils.forceDelete(file);
                }
            }
        }

    }

}
