// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PDSFileUnzipSupport {

    private static final Logger LOG = LoggerFactory.getLogger(PDSFileUnzipSupport.class);

    public class UnzipResult {
        private int extractedFilesCount;
        private int createdFoldersCount;

        private String sourceLocation;
        private String targetLocation;

        public int getExtractedFilesCount() {
            return extractedFilesCount;
        }

        public int getCreatedFoldersCount() {
            return createdFoldersCount;
        }

        public String getSourceLocation() {
            return sourceLocation;
        }

        public String getTargetLocation() {
            return targetLocation;
        }
    }

    public UnzipResult unzipArchive(File file, File destDir) throws IOException {
        UnzipResult unzipResult = new UnzipResult();
        if (!file.exists()) {
            LOG.error("cannot unzip {} because zip file does not exist!", file.getAbsolutePath());
            return unzipResult;
        }
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        unzipResult.targetLocation = destDir.getAbsolutePath();
        unzipResult.sourceLocation = file.getAbsolutePath();

        LOG.debug("start unzipping of {} into {}", unzipResult.sourceLocation, unzipResult.targetLocation);

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {

            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {

                File newFile = newFile(unzipResult, destDir, zipEntry);

                copy(zis, newFile, unzipResult);
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
        return unzipResult;
    }

    private void copy(ZipInputStream zis, File newFile, UnzipResult unzipResult) throws FileNotFoundException, IOException {
        LOG.trace("Handle", newFile);

        if (newFile.isDirectory()) {
            /* we do not copy directory entries */
            LOG.trace("Skipped, because directory:{}", newFile);
            return;
        }

        /* create/copy file from zip content */
        ensureParentFoldersCreated(newFile, unzipResult);

        try (FileOutputStream fos = new FileOutputStream(newFile)) {
            LOG.trace("Create:{}", newFile);

            IOUtils.copy(zis, fos);
        }
    }

    private void ensureParentFoldersCreated(File newFile, UnzipResult unzipResult) throws IOException {
        File parentFile = newFile.getParentFile();
        if (parentFile == null) {
            throw new IOException("Parent directory 'null' not acceptable, but was set for new file:" + newFile.getAbsolutePath());
        }
        if (parentFile.exists()) {
            return;
        }
        /*
         * Parent file does not exist/was not created before. Means the ZIP file does
         * not contain directory entries but only file entries with path info, so we
         * must create missing parent folders:
         */
        int countOfMissingDirectories = 0;
        File missing = parentFile;
        while (missing != null && !missing.exists()) {
            countOfMissingDirectories++;
            missing = missing.getParentFile();
        }

        LOG.trace("Will create {} missing directories for parent file:{}", countOfMissingDirectories, parentFile.getAbsolutePath());

        if (!parentFile.mkdirs()) {
            throw new IOException("Was not able to create directories for parent folder:" + parentFile.getAbsolutePath());
        }

        unzipResult.createdFoldersCount += countOfMissingDirectories;
    }

    private File newFile(UnzipResult unzipResult, File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        if (zipEntry.isDirectory()) {
            unzipResult.createdFoldersCount++;

            /* ensure directory exists */
            Files.createDirectories(destFile.toPath());

        } else {

            unzipResult.extractedFilesCount++;
        }

        return destFile;
    }
}
