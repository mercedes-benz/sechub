package com.mercedesbenz.sechub.commons.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveSupport {

    private static final NoInspectorFallbackInspectionResult NO_FILTER_RESULT = new NoInspectorFallbackInspectionResult();

    private static final Logger LOG = LoggerFactory.getLogger(ArchiveSupport.class);

    public void extractTar(InputStream inputStream, String identifier, File outputDir) throws IOException {
        extractTar(inputStream, identifier, outputDir, null);
    }

    public void extractTar(InputStream inputStream, String identifier, File outputDir, ArchivePathInspector inspector) throws IOException {
        try (ArchiveInputStream archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream("tar", inputStream)) {

            extract(archiveInputStream, identifier, outputDir, inspector);

        } catch (ArchiveException e) {
            throw new IOException("Was not able to extract tar:" + identifier + " at " + outputDir, e);
        }

    }

    public boolean isZipFile(Path pathToFile) {
        try (ZipFile zipFile = new ZipFile(pathToFile.toFile())) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isZipFileStream(InputStream inputStream) {
        if (inputStream == null) {
            return false;
        }
        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            boolean isZipped = zis.getNextEntry() != null;
            return isZipped;
        } catch (IOException e) {
            // only interesting for debugging - normally it is just no ZIP file.
            LOG.debug("The zip file check did fail", e);
            return false;
        }
    }

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

    protected void extract(ArchiveInputStream archiveInputStream, String identifier, File outputDir, ArchivePathInspector inspector)
            throws ArchiveException, IOException {
        if (!(archiveInputStream instanceof TarArchiveInputStream)) {
            throw new IOException("Cannot extract: " + identifier + " because it is not a tar tar");
        }

        ArchiveEntry entry = null;
        while ((entry = archiveInputStream.getNextEntry()) != null) {

            ArchivePathInspectionResult result = startInspection(inspector, entry);
            if (result == null) {
                continue;
            }
            if (!result.isAccepted()) {
                continue;
            }
            String name = null;

            if (result.isPathChangeWanted()) {
                name = result.getWantedPath();
            } else {
                name = entry.getName();
            }
            File outputFile = new File(outputDir, name);

            if (entry.isDirectory()) {
                LOG.debug("Write output directory {}.", outputFile.getAbsolutePath());
                if (!outputFile.exists()) {
                    if (!outputFile.mkdirs()) {
                        throw new IOException("Was not able to create directory:" + outputFile.getAbsolutePath());
                    }
                }
            } else {
                LOG.info("Creating output file: ", outputFile.getAbsolutePath());
                try (OutputStream outputFileStream = new FileOutputStream(outputFile)) {
                    IOUtils.copy(archiveInputStream, outputFileStream);
                }
            }
        }
    }

    private static class NoInspectorFallbackInspectionResult extends ArchivePathInspectionResult {
        @Override
        public boolean isPathChangeWanted() {
            return false; // never
        }

        @Override
        public boolean isAccepted() {
            return true; // always
        }
    }

    private ArchivePathInspectionResult startInspection(ArchivePathInspector filter, ArchiveEntry entry) {
        if (filter == null) {
            return NO_FILTER_RESULT;
        }
        return filter.inspect(entry.getName());
    }

}
