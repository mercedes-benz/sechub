package com.mercedesbenz.sechub.commons.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
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

    private static final Logger LOG = LoggerFactory.getLogger(ArchiveSupport.class);

    private static final KeepAsIsTransformationData DO_NOT_TRANSFORM = new KeepAsIsTransformationData();

    public ArchiveExtractionResult extractTar(InputStream sourceInputStream, String sourceLocation, File outputDir,
            SecHubFileStructureConfiguration configuration) throws IOException {
        try (ArchiveInputStream archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream("tar", sourceInputStream)) {
            if (!(archiveInputStream instanceof TarArchiveInputStream)) {
                throw new IOException("Cannot extract: " + sourceLocation + " because it is not a tar tar");
            }
            return extract(archiveInputStream, sourceLocation, outputDir, configuration);

        } catch (ArchiveException e) {
            throw new IOException("Was not able to extract tar:" + sourceLocation + " at " + outputDir, e);
        }

    }

    public ArchiveExtractionResult extractZip(File file, File destDir, SecHubFileStructureConfiguration configuration) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return extractZip(fis, file.getAbsolutePath(), destDir, configuration);
        }
    }

    public ArchiveExtractionResult extractZip(InputStream sourceInputStream, String sourceLocation, File outputDir,
            SecHubFileStructureConfiguration configuration) throws IOException {
        try (ArchiveInputStream archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream("zip", sourceInputStream)) {

            return extract(archiveInputStream, sourceLocation, outputDir, configuration);

        } catch (ArchiveException e) {
            throw new IOException("Was not able to extract tar:" + sourceLocation + " at " + outputDir, e);
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

    private ArchiveExtractionResult extract(ArchiveInputStream sourceArchiveInputStream, String sourceLocation, File outputDir,
            SecHubFileStructureConfiguration configuration) throws ArchiveException, IOException {

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
                LOG.debug("Write output directory {}.", outputFile.getAbsolutePath());
                if (!outputFile.exists()) {
                    result.createdFoldersCount++;
                    if (!outputFile.mkdirs()) {
                        throw new IOException("Was not able to create directory:" + outputFile.getAbsolutePath());
                    }
                }
            } else {
                LOG.debug("Creating output file: ", outputFile.getAbsolutePath());
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

    private ArchiveTransformationData createTransformationData(SecHubFileStructureConfiguration configuration, String path) {
        if (configuration == null) {
            return DO_NOT_TRANSFORM;
        }
        return ArchiveTransformationDataFactory.create(configuration, path);
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
