// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import static java.util.Objects.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveSupport {

    private static final KeepAsIsTransformationData DO_NOT_TRANSFORM = new KeepAsIsTransformationData();
    private static final Logger LOG = LoggerFactory.getLogger(ArchiveSupport.class);

    private ArchiveTransformationDataFactory archiveTransformationDataFactory;

    public ArchiveSupport() {
        this.archiveTransformationDataFactory = new ArchiveTransformationDataFactory();
    }

    public ArchiveExtractionResult extract(ArchiveType type, InputStream sourceInputStream, String sourceLocation, File outputDir,
            SecHubFileStructureDataProvider configuration) throws IOException {
        if (type == null) {
            throw new IllegalArgumentException("archive type must be defined!");
        }
        switch (type) {
        case TAR:
            return extractTar(sourceInputStream, sourceLocation, outputDir, configuration);
        case ZIP:
            return extractZip(sourceInputStream, sourceLocation, outputDir, configuration);
        default:
            throw new IllegalArgumentException("archive type " + type + " is not supported");

        }
    }

    public void compressFolder(ArchiveType type, File folder, File targetArchiveFile) throws IOException {
        requireNonNull(type, "type may not be null!");
        requireNonNull(folder, "folder may not be null!");
        requireNonNull(targetArchiveFile, "targetArchiveFile may not be null!");

        if (!folder.exists()) {
            throw new FileNotFoundException("Folder does not exist:" + folder);
        }

        if (targetArchiveFile.exists()) {
            Files.delete(targetArchiveFile.toPath());
        }

        // ensure parent file exists
        File parentFile = targetArchiveFile.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }

        try (ArchiveOutputStream outputStream = new ArchiveStreamFactory().createArchiveOutputStream(type.getType(), new FileOutputStream(targetArchiveFile))) {
            String basePath = folder.getAbsolutePath();

            compressRecursively(basePath, outputStream, folder, type);

            outputStream.closeArchiveEntry();

            LOG.debug("Compressed folder: {} into {} archive: {}", folder, type, targetArchiveFile);

        } catch (ArchiveException e) {
            throw new IOException("Was not able to compress: " + folder, e);
        }

    }

    private void compressRecursively(String basePath, ArchiveOutputStream outputStream, File folder, ArchiveType type) throws IOException {
        for (File child : folder.listFiles()) {
            if (child.isDirectory()) {

                compressRecursively(basePath, outputStream, child, type);

            } else {

                String parentPath = child.getParentFile().getAbsolutePath();
                String relativeFromBasePath = parentPath.substring(basePath.length());
                relativeFromBasePath = relativeFromBasePath + "/" + child.getName();

                ArchiveEntry entry = null;
                switch (type) {
                case TAR:
                    entry = new TarArchiveEntry(child, relativeFromBasePath);
                    break;
                case ZIP:
                    entry = new ZipArchiveEntry(child, relativeFromBasePath);
                    break;
                default:
                    throw new IllegalStateException("Unsupported type:" + type);
                }
                outputStream.putArchiveEntry(entry);
            }
        }

    }

    private ArchiveExtractionResult extractTar(InputStream sourceInputStream, String sourceLocation, File outputDir,
            SecHubFileStructureDataProvider configuration) throws IOException {
        try (ArchiveInputStream archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream("tar", sourceInputStream)) {
            if (!(archiveInputStream instanceof TarArchiveInputStream)) {
                throw new IOException("Cannot extract: " + sourceLocation + " because it is not a tar tar");
            }
            return extract(archiveInputStream, sourceLocation, outputDir, configuration);

        } catch (ArchiveException e) {
            throw new IOException("Was not able to extract tar:" + sourceLocation + " at " + outputDir, e);
        }

    }

    private ArchiveExtractionResult extractZip(InputStream sourceInputStream, String sourceLocation, File outputDir,
            SecHubFileStructureDataProvider configuration) throws IOException {
        try (ArchiveInputStream archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream("zip", sourceInputStream)) {

            return extract(archiveInputStream, sourceLocation, outputDir, configuration);

        } catch (ArchiveException e) {
            throw new IOException("Was not able to extract tar:" + sourceLocation + " at " + outputDir, e);
        }

    }

    /**
     * Checks if given stream can be handled as an input stream. Marked as
     * deprecated, because the check will not validate content, data sections etc.
     * As long as source code service uses this method we will keep it, but no other
     * location should not use the implementation.
     *
     * @param inputStream
     * @return true when strip can be handled as a zip input stream
     *
     */
    @Deprecated
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

    private ArchiveExtractionResult extract(ArchiveInputStream sourceArchiveInputStream, String sourceLocation, File outputDir,
            SecHubFileStructureDataProvider configuration) throws ArchiveException, IOException {

        ArchiveExtractionResult result = new ArchiveExtractionResult();
        result.targetLocation = outputDir.getAbsolutePath();
        result.sourceLocation = sourceLocation;

        ArchiveEntry entry = null;
        while ((entry = sourceArchiveInputStream.getNextEntry()) != null) {
            String name = entry.getName();
            if (name == null) {
                throw new IllegalStateException("Entry path is null - cannot be handled!");
            }

            ArchiveTransformationData data = createTransformationData(configuration, name);
            if (data == null) {
                continue;
            }
            if (!data.isAccepted()) {
                continue;
            }
            if (data.isPathChangeWanted()) {
                name = data.getChangedPath();

                if (name == null) {
                    throw new IllegalStateException("Wanted path is null - cannot be handled!");
                }
            }
            File outputFile = new File(outputDir, name);

            if (entry.isDirectory()) {
                LOG.debug("Write output directory: {}", outputFile.getAbsolutePath());
                if (!outputFile.exists()) {
                    result.createdFoldersCount++;
                    if (!outputFile.mkdirs()) {
                        throw new IOException("Was not able to create directory: " + outputFile.getAbsolutePath());
                    }
                }
            } else {
                LOG.debug("Creating output file: {}", outputFile.getAbsolutePath());
                ensureParentFolderExists(outputFile, result);
                if (outputFile.isDirectory()) {
                    continue;
                }
                try (OutputStream outputFileStream = new FileOutputStream(outputFile)) {
                    IOUtils.copy(sourceArchiveInputStream, outputFileStream);
                    result.extractedFilesCount++;
                }
            }
        }
        return result;
    }

    private ArchiveTransformationData createTransformationData(SecHubFileStructureDataProvider dataProvider, String path) {
        if (dataProvider == null) {
            return DO_NOT_TRANSFORM;
        }
        return archiveTransformationDataFactory.create(dataProvider, path);
    }

    private void ensureParentFolderExists(File outputFile, ArchiveExtractionResult result) throws IOException {
        File parentFile = outputFile.getParentFile();
        if (parentFile == null) {
            LOG.error("No parent folder defined");
            return;
        }
        if (parentFile.exists()) {
            // parent folder already exists
            return;
        }
        int count = 0;
        File currentFile = parentFile;
        while (currentFile != null) {
            count++;
            File parentFile2 = currentFile.getParentFile();
            if (parentFile2.exists()) {
                break;
            }
            currentFile = parentFile2;
        }
        if (!parentFile.mkdirs()) {
            throw new IOException("Was not able to create parent folder:" + parentFile.getAbsolutePath());
        } else {
            result.createdFoldersCount += count;
        }

    }

    public enum ArchiveType {
        ZIP("zip"),

        TAR("tar");

        private String type;

        private ArchiveType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    private static class KeepAsIsTransformationData implements ArchiveTransformationData {

        private KeepAsIsTransformationData() {
        }

        @Override
        public boolean isPathChangeWanted() {
            return false; // never
        }

        @Override
        public String getChangedPath() {
            return null; // never necessary, because we never want a path change
        }

        @Override
        public boolean isAccepted() {
            return true; // always
        }

    }

}
