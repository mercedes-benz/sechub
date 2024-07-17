// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class DirectoryAndFileSupport {

    /**
     * Cleans all unwanted content recursively starting with parent folder. Each
     * file or directory which is accepted by the given filter will be deleted.
     *
     * @param parentDirectory              the directory to start from
     * @param filterAcceptingFilesToDelete filter which accepts only files to delete
     * @throws IOException
     */
    public void cleanDirectories(File parentDirectory, FileFilter filterAcceptingFilesToDelete) throws IOException {
        if (parentDirectory == null) {
            throw new IllegalArgumentException("Parent folder may not be null!");
        }
        cleanDirectoriesRecursive(parentDirectory, filterAcceptingFilesToDelete);
    }

    private void cleanDirectoriesRecursive(File parentDirectory, FileFilter filter) throws IOException {
        File[] files = parentDirectory.listFiles();

        for (File file : files) {
            handleFileOrDirectoryRecursive(filter, file);
        }

    }

    private void handleFileOrDirectoryRecursive(FileFilter filter, File file) throws IOException {
        if (file.isDirectory()) {
            if (filter.accept(file)) {
                /* delete the sub directory recursive */
                FileUtils.forceDelete(file);
            } else {
                /* not accepted to delete, but inspect children */
                cleanDirectoriesRecursive(file, filter);
            }
        } else {
            /* not directory */
            if (filter.accept(file)) {
                FileUtils.forceDelete(file);
            }
        }
    }

}
