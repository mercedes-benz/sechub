// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.archive;

import com.mercedesbenz.sechub.commons.archive.ArchiveCreationContext.CreationPathContext;
import com.mercedesbenz.sechub.commons.model.*;
import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipInputStream;

import static java.util.Objects.requireNonNull;

public class ArchiveSupport {

    private static final KeepAsIsTransformationData DO_NOT_TRANSFORM = new KeepAsIsTransformationData();
    private static final Logger LOG = LoggerFactory.getLogger(ArchiveSupport.class);

    private final ArchiveTransformationDataFactory archiveTransformationDataFactory;
    private boolean createMissingFiles;

    public ArchiveSupport() {
        archiveTransformationDataFactory = new ArchiveTransformationDataFactory();
    }

    /**
     * Extract given archive type to output directory by
     *
     * @param archiveType
     * @param sourceInputStream
     * @param sourceLocation            the path for the given source input stream.
     *                                  This information is only used in case of
     *                                  failures to print out the location - means
     *                                  only for debugging/error handling
     * @param outputDir
     * @param fileStructureDataProvider used to transform/filter the extraction. If
     *                                  <code>null</code>, a fallback will be used
     *                                  which does no transformation or filtering
     *
     * @return extraction result
     *
     * @throws IOException
     */
    public ArchiveExtractionResult extract(ArchiveType archiveType,
                                           InputStream sourceInputStream,
                                           String sourceLocation,
                                           File outputDir,
                                           SecHubFileStructureDataProvider fileStructureDataProvider,
                                           ArchiveExtractionContext archiveExtractionContext
                                           ) throws IOException {
        if (archiveType == null) {
            throw new IllegalArgumentException("archive type must be defined!");
        }
        switch (archiveType) {
        case TAR:
            return extractTar(sourceInputStream, sourceLocation, outputDir, fileStructureDataProvider, archiveExtractionContext);
        case ZIP:
            return extractZip(sourceInputStream, sourceLocation, outputDir, fileStructureDataProvider, archiveExtractionContext);
        default:
            throw new IllegalArgumentException("archive type " + archiveType + " is not supported");

        }
    }

    public void setCreateMissingFiles(boolean createPseudoFilesForMissingFiles) {
        createMissingFiles = createPseudoFilesForMissingFiles;
    }

    /**
     * Creates all necessary archives for a given SecHub configuration
     *
     * @param configuration    the sechub configuration
     * @param workingDirectory the directory where the configuration relative paths
     *                         starts from
     * @param targetFolder     the directory where the archives shall be created. If
     *                         not existing and archive files are created, the
     *                         target folder will be created automatically.
     * @return {@link ArchivesCreationResult}
     * @throws IOException
     */
    public ArchivesCreationResult createArchives(SecHubConfigurationModel configuration, Path workingDirectory, Path targetFolder) throws IOException {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration may not be null!");
        }
        if (workingDirectory == null) {
            throw new IllegalArgumentException("workingDirectory may not be null!");
        }
        if (targetFolder == null) {
            throw new IllegalArgumentException("targetFolder may not be null!");
        }

        ArchivesCreationResult result = new ArchivesCreationResult();
        try {
            File sourceArchiveFile = createArchive(ArchiveType.ZIP, configuration, workingDirectory, targetFolder);
            if (sourceArchiveFile != null) {
                result.sourceArchiveFile = sourceArchiveFile.toPath();
            }

            File binaryArchiveFile = createArchive(ArchiveType.TAR, configuration, workingDirectory, targetFolder);
            if (binaryArchiveFile != null) {
                result.binaryArchiveFile = binaryArchiveFile.toPath();
            }
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Creation of archives failed");
            if (e instanceof IOException) {
                sb.append(" - ").append(e.getMessage());
            }
            try {
                deleteArchives(result);
            } catch (Exception cleanupFailure) {
                sb.append(" (and auto cleanup of tempoary data also failed: ");
                sb.append(cleanupFailure.getMessage());
                sb.append(")");
            }
            throw new IOException(sb.toString(), e);
        }
        return result;
    }

    public void deleteArchives(ArchivesCreationResult creationResult) throws IOException {
        if (creationResult.isBinaryArchiveCreated()) {
            Files.deleteIfExists(creationResult.getBinaryArchiveFile());
        }
        if (creationResult.isSourceArchiveCreated()) {
            Files.deleteIfExists(creationResult.getSourceArchiveFile());
        }
    }

    private File createArchive(ArchiveType archiveType, SecHubConfigurationModel configuration, Path workingDirectory, Path targetFolder) throws IOException {
        ArchiveCreationContext creationContext = new ArchiveCreationContext(archiveType, targetFolder);
        collectLegacySetupForCodeScan(configuration, creationContext);
        collectDataParts(configuration, creationContext);

        if (creationContext.isEmpty()) {
            return null;
        }
        File archiveFile = creationContext.getArchiveFile();
        LOG.debug("Start creating {} archive: {}", archiveType, archiveFile);

        try (ArchiveOutputStream outputStream = new ArchiveStreamFactory().createArchiveOutputStream(archiveType.getType(),
                new FileOutputStream(archiveFile))) {

            if (outputStream instanceof @SuppressWarnings("resource")TarArchiveOutputStream tarOutputStream) {
                tarOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
                tarOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
            }

            Map<String, CreationPathContext> uniqueNameToPaths = creationContext.getUniqueToPathsMap();
            Path workingDirectoryRealPath = workingDirectory.toRealPath();

            for (Entry<String, CreationPathContext> entry : uniqueNameToPaths.entrySet()) {

                String uniqueName = entry.getKey();
                CreationPathContext creationPathContext = entry.getValue();
                Set<String> paths = creationPathContext.getPaths();

                for (String pathAsStringFromConfiguration : paths) {

                    File file = null;
                    Path pathFromConfig = Paths.get(pathAsStringFromConfiguration);
                    if (pathFromConfig.isAbsolute()) {
                        if (!pathFromConfig.startsWith(workingDirectoryRealPath)) {
                            throw new FileNotFoundException(
                                    "The absolute path: " + pathAsStringFromConfiguration + " is not part of inside " + workingDirectoryRealPath);
                        }
                        file = pathFromConfig.toFile();

                    } else {
                        file = new File(workingDirectoryRealPath.toFile(), pathAsStringFromConfiguration);
                        if (!file.exists()) {
                            if (createMissingFiles) {
                                file.getParentFile().mkdirs();
                                Files.createFile(file.toPath());
                            } else {
                                throw new FileNotFoundException("Did not found: " + pathAsStringFromConfiguration + " inside " + workingDirectory);
                            }
                        }
                    }

                    String pathAddition;
                    if (ArchiveCreationContext.LEGACY_IDENTIFIER_UNIQUE_NAME.equals(uniqueName)) {
                        pathAddition = null;
                    } else {
                        pathAddition = ArchiveConstants.DATA_SECTION_FOLDER + uniqueName;
                    }
                    compressRecursively(workingDirectoryRealPath.toString(), outputStream, file, archiveType, pathAddition, creationPathContext);
                    LOG.debug("Create archive entry with relative prefix: {}. Origin file/folder: {}", pathAddition, file);
                }

            }

            outputStream.finish();

        } catch (ArchiveException e) {
            throw new IOException("Was not able to create: " + archiveFile, e);
        }

        return archiveFile;
    }

    private void collectDataParts(SecHubConfigurationModel configuration, ArchiveCreationContext creationContext) {
        if (configuration.getData().isEmpty()) {
            return;
        }
        SecHubDataConfiguration data = configuration.getData().get();

        switch (creationContext.getArchiveType()) {
        case TAR:
            collectBaseFoldersAndFiles(data.getBinaries(), creationContext);
            break;
        case ZIP:
            collectBaseFoldersAndFiles(data.getSources(), creationContext);
            break;
        default:
            LOG.error("Unsupported archive type: {}", creationContext.getArchiveType());
            break;
        }
    }

    private void collectBaseFoldersAndFiles(List<? extends SecHubFileSystemContainer> fileSystemContainers, ArchiveCreationContext creationContext) {
        for (SecHubFileSystemContainer fileSystemContainer : fileSystemContainers) {
            if (fileSystemContainer.getFileSystem().isEmpty()) {
                continue;
            }
            String uniqueName = ArchiveCreationContext.LEGACY_IDENTIFIER_UNIQUE_NAME;
            if (fileSystemContainer instanceof SecHubDataConfigurationObject configObject) {
                uniqueName = configObject.getUniqueName();
            } else {
                LOG.warn("No unique name found inside a data section entry - should not happen! Will use legacy identifier as fallback!");
            }
            SecHubFileSystemConfiguration fileSystem = fileSystemContainer.getFileSystem().get();
            creationContext.addBaseFolderOrFilePaths(uniqueName, fileSystem.getFiles(), fileSystemContainer.getIncludes(), fileSystemContainer.getExcludes());
            creationContext.addBaseFolderOrFilePaths(uniqueName, fileSystem.getFolders(), fileSystemContainer.getIncludes(), fileSystemContainer.getExcludes());
        }

    }

    private void collectLegacySetupForCodeScan(SecHubConfigurationModel configuration, ArchiveCreationContext creationContext) {
        if (!ArchiveType.ZIP.equals(creationContext.getArchiveType())) {
            return;
        }
        if (configuration.getCodeScan().isEmpty()) {
            return;
        }
        SecHubCodeScanConfiguration codeScan = configuration.getCodeScan().get();
        if (codeScan.getFileSystem().isEmpty()) {
            return;
        }
        SecHubFileSystemConfiguration fileSystem = codeScan.getFileSystem().get();
        creationContext.addBaseFolderOrFilePaths(ArchiveCreationContext.LEGACY_IDENTIFIER_UNIQUE_NAME, fileSystem.getFiles(), codeScan.getIncludes(),
                codeScan.getExcludes());
        creationContext.addBaseFolderOrFilePaths(ArchiveCreationContext.LEGACY_IDENTIFIER_UNIQUE_NAME, fileSystem.getFolders(), codeScan.getIncludes(),
                codeScan.getExcludes());

    }

    /**
     * Simple archive compression - does only compress a given folder to a given
     * target file. No special data structure transformation.
     *
     * @param type
     * @param folder
     * @param targetArchiveFile
     * @throws IOException
     */
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
            String basePath = folder.toPath().toRealPath().toString();

            if (outputStream instanceof @SuppressWarnings("resource")TarArchiveOutputStream tarArchiveOutputStream) {
                /* in this case we activate long file support */
                tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            }
            compressRecursively(basePath, outputStream, folder, type, null, null);

            outputStream.finish();

            LOG.debug("Compressed folder: {} into {} archive: {}", folder, type, targetArchiveFile);

        } catch (ArchiveException e) {
            throw new IOException("Was not able to compress: " + folder, e);
        }

    }

    private void compressRecursively(String basePath, ArchiveOutputStream outputStream, File file, ArchiveType type, String pathAddition,
            CreationPathContext creationPathContext) throws IOException {

        if (creationPathContext != null) {
            if (creationPathContext.isExcluded(file)) {
                /* shall be excluded */
                if (!creationPathContext.isIncluded(file)) {
                    /* exclude not overriden by include filter */
                    return;
                }
            }
        }
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                compressRecursively(basePath, outputStream, child, type, pathAddition, creationPathContext);
            }
        } else {
            String relativePath;

            String parentPath = file.getParentFile().toPath().toRealPath().toString();
            String relativeFromBasePath = parentPath.substring(basePath.length());
            relativePath = (pathAddition == null ? "" : pathAddition) + relativeFromBasePath + "/" + file.getName();
            if (relativePath.isEmpty()) {
                LOG.error("Relative path is empty");
                return;
            }
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }

            Path.of(relativePath).toAbsolutePath();
            if (LOG.isTraceEnabled()) {
                /* @formatter:off */
                    String logText = """
                        Compress
                            - pathAddition         :{}
                            - basePath             :{}
                            - parentPath           :{}
                            - file to compress     :{}
                            - relativeFromBasePath :{}
                            - relativePath         :{}
                            """;

                    LOG.trace(logText,
                            pathAddition,
                            basePath,
                            parentPath,
                            file,
                            relativeFromBasePath,
                            relativePath
                            );
                    /* @formatter:on */
            }

            ArchiveEntry entry = null;
            switch (type) {
            case TAR:
                entry = new TarArchiveEntry(file, relativePath);
                break;
            case ZIP:
                entry = new ZipArchiveEntry(file, relativePath);
                break;
            default:
                throw new IllegalStateException("Unsupported type:" + type);
            }
            /* write archive entry */
            outputStream.putArchiveEntry(entry);

            /* write entry data */
            try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                IOUtils.copy(inputStream, outputStream);
            }
            outputStream.closeArchiveEntry();
        }

    }

    private ArchiveExtractionResult extractTar(InputStream sourceInputStream, String sourceLocation, File outputDir,
                                               SecHubFileStructureDataProvider fileStructureProvider, ArchiveExtractionContext properties) throws IOException {
        try (ArchiveInputStream archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream("tar", sourceInputStream)) {
            if (!(archiveInputStream instanceof TarArchiveInputStream)) {
                throw new IOException("Cannot extract: " + sourceLocation + " because it is not a tar tar");
            }

            SafeArchiveInputStream safeArchiveInputStream = new SafeArchiveInputStream(archiveInputStream, properties);
            return extract(safeArchiveInputStream, sourceLocation, outputDir, fileStructureProvider);

        } catch (ArchiveException e) {
            throw new IOException("Was not able to extract tar:" + sourceLocation + " at " + outputDir, e);
        }

    }

    private ArchiveExtractionResult extractZip(InputStream sourceInputStream, String sourceLocation, File outputDir,
                                               SecHubFileStructureDataProvider configuration, ArchiveExtractionContext properties) throws IOException {
        try (ArchiveInputStream archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream("zip", sourceInputStream)) {

            SafeArchiveInputStream safeArchiveInputStream = new SafeArchiveInputStream(archiveInputStream, properties);
            return extract(safeArchiveInputStream, sourceLocation, outputDir, configuration);

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

    private ArchiveExtractionResult extract(SafeArchiveInputStream safeArchiveInputStream, String sourceLocation, File outputDir,
            SecHubFileStructureDataProvider fileStructureProvider) throws ArchiveException, IOException {

        ArchiveExtractionResult result = new ArchiveExtractionResult();
        result.targetLocation = outputDir.getAbsolutePath();
        result.sourceLocation = sourceLocation;

        ArchiveEntry entry = null;
        while ((entry = safeArchiveInputStream.getNextEntry()) != null) {
            String name = entry.getName();
            if (name == null) {
                throw new IllegalStateException("Entry path is null - cannot be handled!");
            }

            ArchiveTransformationData data = createTransformationData(fileStructureProvider, name);
            if (data == null) {
                continue;
            }
            if (!data.isAccepted()) {
                LOG.debug("Filtering: {}", name);

                continue;
            }
            if (data.isPathChangeWanted()) {
                name = data.getChangedPath();

                LOG.debug("Path changed to: {}", name);

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
                    IOUtils.copy(safeArchiveInputStream, outputFileStream);
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

        private final String type;

        ArchiveType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public class ArchivesCreationResult {

        private Path sourceArchiveFile;
        private Path binaryArchiveFile;

        public Path getBinaryArchiveFile() {
            return binaryArchiveFile;
        }

        public Path getSourceArchiveFile() {
            return sourceArchiveFile;
        }

        public boolean isBinaryArchiveCreated() {
            return binaryArchiveFile != null && Files.exists(binaryArchiveFile);
        }

        public boolean isSourceArchiveCreated() {
            return sourceArchiveFile != null && Files.exists(sourceArchiveFile);
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
