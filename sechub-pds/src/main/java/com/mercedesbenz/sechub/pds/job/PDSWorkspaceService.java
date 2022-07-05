// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.archive.ArchiveExtractionResult;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport.ArchiveType;
import com.mercedesbenz.sechub.commons.archive.SecHubFileStructureDataProvider;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelSupport;
import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;
import com.mercedesbenz.sechub.pds.PDSNotFoundException;
import com.mercedesbenz.sechub.pds.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.pds.storage.PDSMultiStorageService;
import com.mercedesbenz.sechub.pds.storage.PDSStorageInfoCollector;
import com.mercedesbenz.sechub.pds.util.PDSArchiveSupportProvider;
import com.mercedesbenz.sechub.storage.core.JobStorage;

@Service
public class PDSWorkspaceService {

    public static final String UPLOAD = "upload";
    public static final String EXTRACTED = "extracted";
    public static final String EXTRACTED_SOURCES = EXTRACTED + "/sources";
    public static final String EXTRACTED_BINARIES = EXTRACTED + "/binaries";

    public static final String OUTPUT = "output";
    public static final String MESSAGES = "messages";
    public static final String RESULT_TXT = "result.txt";
    public static final String METADATA_TXT = "metadata.txt";

    public static final String SYSTEM_OUT_LOG = "system-out.log";
    public static final String SYSTEM_ERROR_LOG = "system-error.log";

    private static final Logger LOG = LoggerFactory.getLogger(PDSWorkspaceService.class);
    private static final String WORKSPACE_PARENT_FOLDER_PATH = "./";

    @PDSMustBeDocumented(value = "Set pds workspace root folder path. Inside this path the sub directory `workspace` will be created.", scope = "execution")
    @Value("${sechub.pds.workspace.rootfolder:" + WORKSPACE_PARENT_FOLDER_PATH + "}")
    String workspaceRootFolderPath = WORKSPACE_PARENT_FOLDER_PATH;

    @Autowired
    PDSMultiStorageService storageService;

    @Autowired
    PDSServerConfigurationService serverConfigService;

    @Autowired
    PDSArchiveSupportProvider archiveSupportProvider;

    @Autowired
    PDSStorageInfoCollector storageInfoCollector;

    @PDSMustBeDocumented(value = "Defines if workspace is automatically cleaned when no longer necessary - means launcher script has been executed and finished (failed or done)", scope = "execution")
    @Value("${sechub.pds.workspace.autoclean.disabled:false}")
    private boolean workspaceAutoCleanDisabled;

    private static final ArchiveFilter TAR_FILE_FILTER = new TarFileFilter();

    private static final ArchiveFilter ZIP_FILE_FILTER = new SourcecodeZipFileFilter();

    private SecHubConfigurationModelSupport modelSupport = new SecHubConfigurationModelSupport();

    /**
     * Prepares workspace:
     * <ol>
     * <li><Fetch data from storage and copy to local workspace</li>
     * </ol>
     *
     * @param config
     * @param string
     */
    public void prepareWorkspace(UUID jobUUID, PDSJobConfiguration config, String metaData) throws IOException {

        PDSJobConfigurationSupport configurationSupport = new PDSJobConfigurationSupport(config);

        PreparationContext preparationContext = createPreparationContext(config, configurationSupport);

        if (metaData != null && !metaData.isEmpty()) {
            File metaDataFile = getMetaDataFile(jobUUID);
            LOG.debug("Meta data found for PDS job {} - will create metadata file {}", jobUUID, metaDataFile);

            TextFileWriter writer = new TextFileWriter();
            writer.save(metaDataFile, metaData, true);
            LOG.info("Created meta data file for PDS job {}", jobUUID);
        }

        File jobFolder = getUploadFolder(jobUUID);
        JobStorage storage = fetchStorage(jobUUID, config);
        Set<String> names = storage.listNames();

        LOG.debug("For jobUUID={} following names are found in storage:{}", jobUUID, names);
        for (String name : names) {
            if (isWantedStorageContent(name, configurationSupport, preparationContext)) {

                InputStream fetchedInputStream = storage.fetch(name);
                File uploadFile = new File(jobFolder, name);

                try {
                    FileUtils.copyInputStreamToFile(fetchedInputStream, uploadFile);
                    LOG.debug("Imported '{}' for job {} from storage to {}", name, jobUUID, uploadFile.getAbsolutePath());
                } catch (IOException e) {
                    LOG.error("Was not able to import {} for job {}, reason:", name, jobUUID, e.getMessage());
                    throw new IllegalArgumentException("Cannot import given file from storage", e);
                }
            } else {
                LOG.debug("Did NOT import '{}' for job {} from storage - was not wanted", name, jobUUID);
            }

        }

    }

    private PreparationContext createPreparationContext(PDSJobConfiguration config, PDSJobConfigurationSupport configurationSupport) {

        PreparationContext preparationContext = new PreparationContext();

        SecHubConfigurationModel model = configurationSupport.resolveSecHubConfigurationModel();

        if (model != null) {
            PDSProductSetup productSetup = serverConfigService.getProductSetupOrNull(config.getProductId());
            if (productSetup == null) {
                throw new IllegalStateException("PDS product setup for " + config.getProductId() + " not found!");
            }
            ScanType scanType = null;
            if (productSetup != null) {
                scanType = productSetup.getScanType();
            }
            if (scanType == null) {
                throw new IllegalStateException("PDS product setup for " + config.getProductId() + " has no scan type defined!");
            }
            preparationContext.binaryAccepted = modelSupport.isBinaryRequired(scanType, model);
            preparationContext.sourceAccepted = modelSupport.isSourceRequired(scanType, model);

        } else {
            /*
             * necessary when PDS has been executed without SecHub - e.g. for testing. There
             * is no model available, so we must accept everything.
             */
            preparationContext.binaryAccepted = true;
            preparationContext.sourceAccepted = true;

        }
        return preparationContext;
    }

    private boolean isWantedStorageContent(String name, PDSJobConfigurationSupport configurationSupport, PreparationContext preparationContext) {
        if (FILENAME_SOURCECODE_ZIP.equals(name)) {
            if (preparationContext.isSourceAccepted()) {
                LOG.debug("Sourcecode zip file found and will not be ignored");
                return true;
            }
            LOG.debug("Sourcecode zip file found but ignored, because preparation context says that sources are not accepted for this job");
            return false;
        }
        if (FILENAME_BINARIES_TAR.equals(name)) {
            if (preparationContext.isBinaryAccepted()) {
                LOG.debug("Binaries tar file found and will not be ignored");
                return true;
            }
            LOG.debug("Binaries tar file found but ignored, because preparation context says that binaries are not accepted for this job");
            return false;
        }
        return false;
    }

    private JobStorage fetchStorage(UUID pdsJobUUID, PDSJobConfiguration config) {

        UUID jobUUID;
        String storagePath;
        PDSJobConfigurationSupport configurationSupport = new PDSJobConfigurationSupport(config);

        boolean useSecHubStorage = configurationSupport.isSecHubStorageEnabled();

        if (useSecHubStorage) {
            storagePath = configurationSupport.getSecHubStoragePath();
            jobUUID = config.getSechubJobUUID();
        } else {
            storagePath = null;// will force default storage path for the PDS product
            jobUUID = pdsJobUUID;
        }

        LOG.debug("PDS job {}: feching storage for storagePath = {} and jobUUID:{}, useSecHubStorage={}", pdsJobUUID, storagePath, jobUUID, useSecHubStorage);
        JobStorage storage = storageService.getJobStorage(storagePath, jobUUID);

        storageInfoCollector.informFetchedStorage(storagePath, config.getSechubJobUUID(), pdsJobUUID, storage);

        return storage;
    }

    /**
     * Resolves upload folder - if not existing it will be created
     *
     * @param jobUUID
     * @return upload folder
     */
    public File getUploadFolder(UUID jobUUID) {
        File file = new File(getWorkspaceFolder(jobUUID), UPLOAD);
        file.mkdirs();
        return file;
    }

    private Path getWorkspaceFolderPath(UUID jobUUID) {
        File workspaceFolder = getWorkspaceFolder(jobUUID);
        Path workspaceFolderPath = workspaceFolder.toPath();
        return workspaceFolderPath;
    }

    /**
     * Resolves upload folder - if not existing it will be created
     *
     * @param jobUUID
     * @return upload folder
     * @throws IllegalStateException in case the workspace folder does not exist and
     *                               cannot be created (e.g. because of missing
     *                               permissions)
     */
    public File getWorkspaceFolder(UUID jobUUID) {
        Path jobWorkspacePath = Paths.get(workspaceRootFolderPath, "workspace", jobUUID.toString());
        File jobWorkspaceFolder = jobWorkspacePath.toFile();

        if (!jobWorkspaceFolder.exists()) {
            try {
                Files.createDirectories(jobWorkspacePath);
            } catch (IOException e) {
                throw new IllegalStateException("Was not able to create workspace job folder: " + jobWorkspacePath, e);
            }
        }
        return jobWorkspaceFolder;
    }

    public void extractZipFileUploadsWhenConfigured(UUID jobUUID, PDSJobConfiguration config) throws IOException {
        PDSProductSetup productSetup = resolveProductSetup(config);
        if (!productSetup.isExtractUploads()) {
            return;
        }
        ScanType scanType = productSetup.getScanType();
        SecHubFileStructureDataProvider provider = resolveFileStructureDataProviderOrNull(jobUUID, config, scanType);
        extractUploadedZipFiles(jobUUID, true, provider);
    }

    public void extractTarFileUploadsWhenConfigured(UUID jobUUID, PDSJobConfiguration config) throws IOException {
        PDSProductSetup productSetup = resolveProductSetup(config);
        if (!productSetup.isExtractUploads()) {
            return;
        }
        ScanType scanType = productSetup.getScanType();
        SecHubFileStructureDataProvider provider = resolveFileStructureDataProviderOrNull(jobUUID, config, scanType);
        exractUploadedTarFiles(jobUUID, true, provider);
    }

    SecHubFileStructureDataProvider resolveFileStructureDataProviderOrNull(UUID jobUUID, PDSJobConfiguration config, ScanType scanType) throws IOException {

        SecHubConfigurationModel model = resolveAndEnsureSecHubConfigurationModel(config);
        if (model == null) {
            LOG.warn("No sechub model found - cannot resolve file structure data provider! No filtering will be active.");
            return null;
        }
        PDSJobConfigurationSupport support = new PDSJobConfigurationSupport(config);
        /* @formatter:off */
        SecHubFileStructureDataProvider provider = SecHubFileStructureDataProvider.builder().
                setScanType(scanType).
                setModel(model).
                setExcludedFilePatterns(support.createExcludedFilePatternList()).
                setIncludedFilePatterns(support.createIncludedFilePatternList()).
                build();
        /* @formatter:on */

        return provider;
    }

    private PDSProductSetup resolveProductSetup(PDSJobConfiguration config) {
        PDSProductSetup productSetup = serverConfigService.getProductSetupOrNull(config.getProductId());
        if (productSetup == null) {
            throw new PDSNotFoundException("Product referenced inside Job configuration does not exist at this server, id=" + config.getProductId());
        }
        return productSetup;
    }

    private SecHubConfigurationModel resolveAndEnsureSecHubConfigurationModel(PDSJobConfiguration config) {
        PDSJobConfigurationSupport jobConfigurationSupport = new PDSJobConfigurationSupport(config);
        return jobConfigurationSupport.resolveSecHubConfigurationModel();
    }

    void extractUploadedZipFiles(UUID jobUUID, boolean deleteOriginFiles, SecHubFileStructureDataProvider configuration) throws IOException {
        extractArchives(jobUUID, deleteOriginFiles, configuration, ZIP_FILE_FILTER, EXTRACTED_SOURCES);

    }

    void exractUploadedTarFiles(UUID jobUUID, boolean deleteOriginFiles, SecHubFileStructureDataProvider configuration) throws IOException {
        extractArchives(jobUUID, deleteOriginFiles, configuration, TAR_FILE_FILTER, EXTRACTED_BINARIES);

    }

    private void extractArchives(UUID jobUUID, boolean deleteOriginFiles, SecHubFileStructureDataProvider configuration, ArchiveFilter fileFilter,
            String extractionSubfolder) throws IOException, FileNotFoundException {

        File uploadFolder = getUploadFolder(jobUUID);
        File[] archiveFiles = uploadFolder.listFiles(fileFilter);

        int amountOfFiles = archiveFiles.length;
        LOG.debug("{} *{} file(s) found for job {}", amountOfFiles, fileFilter.getArchiveEnding(), jobUUID);
        if (amountOfFiles == 0) {
            return;
        }

        ArchiveType archiveType = fileFilter.getArchiveType();

        File extractionTargetFolder = new File(uploadFolder, extractionSubfolder);
        if (!extractionTargetFolder.mkdirs()) {
            throw new IOException("Was not able to create " + extractionTargetFolder.getAbsolutePath());
        }

        for (File archiveFile : archiveFiles) {
            try (FileInputStream archiveFileInputStream = new FileInputStream(archiveFile)) {
                ArchiveExtractionResult extractionResult = archiveSupportProvider.getArchiveSupport().extract(archiveType, archiveFileInputStream,
                        archiveFile.getAbsolutePath(), extractionTargetFolder, configuration);

                LOG.info("Extracted {} files to {}", extractionResult.getExtractedFilesCount(), extractionResult.getTargetLocation());

                if (deleteOriginFiles) {
                    LOG.debug("Forcing delete of origin file: {} ", archiveFile.getAbsolutePath());
                    FileUtils.forceDelete(archiveFile);
                }
            }
        }
    }

    public void cleanup(UUID jobUUID, PDSJobConfiguration config) throws IOException {
        FileUtils.deleteDirectory(getWorkspaceFolder(jobUUID));
        LOG.info("Removed workspace folder for job {}", jobUUID);

        PDSJobConfigurationSupport support = new PDSJobConfigurationSupport(config);

        if (support.isSecHubStorageEnabled()) {
            LOG.info("Removed NOT storage for PDS job {} because sechub storage and will be handled by sechub job {}", jobUUID, config.getSechubJobUUID());

        } else {
            JobStorage storage = fetchStorage(jobUUID, config);
            storage.deleteAll();
            LOG.info("Removed storage for job {}", jobUUID);
        }

    }

    public String getProductPathFor(PDSJobConfiguration config) {
        String productId = config.getProductId();
        PDSProductSetup productSetup = serverConfigService.getProductSetupOrNull(productId);
        if (productSetup == null) {
            return null;
        }
        return productSetup.getPath();

    }

    /**
     * When <code>true</code> workspace will not be automatically cleaned up, means
     * upload files results etc. are not deleted after scans!
     *
     * @return <code>true</code> when auto clean shall NOT be done
     */
    public boolean isWorkspaceAutoCleanDisabled() {
        return workspaceAutoCleanDisabled;
    }

    public File getSystemErrorFile(UUID jobUUID) {
        return new File(getOutputFolder(jobUUID), SYSTEM_ERROR_LOG);
    }

    public File getSystemOutFile(UUID jobUUID) {
        return new File(getOutputFolder(jobUUID), SYSTEM_OUT_LOG);
    }

    public File getResultFile(UUID jobUUID) {
        return new File(getOutputFolder(jobUUID), RESULT_TXT);
    }

    public File getMetaDataFile(UUID jobUUID) {
        return new File(getWorkspaceFolder(jobUUID), METADATA_TXT);
    }

    /**
     * Resolves upload folder - if not existing it will be created
     *
     * @param jobUUID
     * @return upload folder
     */
    public File getOutputFolder(UUID jobUUID) {
        File outputFolder = new File(getWorkspaceFolder(jobUUID), OUTPUT);
        outputFolder.mkdirs();
        return outputFolder;
    }

    /**
     * Resolves messages folder - if not existing it will be created
     *
     * @param jobUUID
     * @return upload folder
     */
    public File getMessagesFolder(UUID jobUUID) {
        File outputFolder = new File(getOutputFolder(jobUUID), MESSAGES);
        outputFolder.mkdirs();
        return outputFolder;
    }

    public long getMinutesToWaitForResult(PDSJobConfiguration config) {
        PDSProductSetup productSetup = serverConfigService.getProductSetupOrNull(config.getProductId());
        if (productSetup == null) {
            return -1;
        }
        return productSetup.getMinutesToWaitForProductResult();
    }

    public boolean hasExtractedSources(UUID jobUUID) {
        Path workspaceFolderPath = getWorkspaceFolderPath(jobUUID);
        Path extractedSourcesLocation = createExtractedSourcesLocation(workspaceFolderPath);
        return !isDirectoryEmptyOrDoesNotExist(extractedSourcesLocation);
    }

    public boolean hasExtractedBinaries(UUID jobUUID) {
        Path workspaceFolderPath = getWorkspaceFolderPath(jobUUID);
        Path extractedSourcesLocation = createExtractedBinariesLocation(workspaceFolderPath);
        return !isDirectoryEmptyOrDoesNotExist(extractedSourcesLocation);
    }

    public String getFileEncoding(UUID jobUUID) {
        return "UTF-8"; // currently only UTF-8 expected
    }

    public WorkspaceLocationData createLocationData(UUID jobUUID) {
        Path workspaceFolderPath = getWorkspaceFolderPath(jobUUID);
        WorkspaceLocationData locationData = new WorkspaceLocationData();

        locationData.workspaceLocation = createWorkspacePathAndEnsureParentDirectories(workspaceFolderPath, null).toString();
        locationData.resultFileLocation = createWorkspacePathAndEnsureParentDirectories(workspaceFolderPath, OUTPUT + File.separator + RESULT_TXT).toString();
        locationData.userMessagesLocation = createWorkspacePathAndEnsureDirectory(workspaceFolderPath, OUTPUT + File.separator + MESSAGES).toString();
        locationData.metaDataFileLocation = createWorkspacePathAndEnsureParentDirectories(workspaceFolderPath, METADATA_TXT).toString();

        locationData.extractedSourcesLocation = createExtractedSourcesLocation(workspaceFolderPath).toString();
        locationData.extractedBinariesLocation = createExtractedBinariesLocation(workspaceFolderPath).toString();

        locationData.sourceCodeZipFileLocation = createSourceCodeZipFileLocation(workspaceFolderPath).toString();
        locationData.binariesTarFileLocation = createBinariesTarFileLocation(workspaceFolderPath).toString();

        return locationData;
    }

    private Path createBinariesTarFileLocation(Path workspaceFolderPath) {
        return createWorkspacePathAndEnsureParentDirectories(workspaceFolderPath, UPLOAD + "/" + FILENAME_BINARIES_TAR);
    }

    private Path createSourceCodeZipFileLocation(Path workspaceFolderPath) {
        return createWorkspacePathAndEnsureParentDirectories(workspaceFolderPath, UPLOAD + "/" + FILENAME_SOURCECODE_ZIP);
    }

    private Path createExtractedBinariesLocation(Path workspaceFolderPath) {
        return createWorkspacePathAndEnsureParentDirectories(workspaceFolderPath, UPLOAD + "/" + EXTRACTED_BINARIES);
    }

    private Path createExtractedSourcesLocation(Path workspaceFolderPath) {
        return createWorkspacePathAndEnsureParentDirectories(workspaceFolderPath, UPLOAD + "/" + EXTRACTED_SOURCES);
    }

    private Path createWorkspacePathAndEnsureDirectory(Path workspaceLocation, String subPath) {
        Path path = createWorkspacePathAndEnsureParentDirectories(workspaceLocation, subPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                throw new IllegalStateException("Was not able to create directory: " + path);
            }
        }
        return path;
    }

    private Path createWorkspacePathAndEnsureParentDirectories(Path workspaceLocation, String subPath) {
        Path workspaceChildPath;
        if (subPath == null) {
            workspaceChildPath = workspaceLocation;
        } else {
            workspaceChildPath = workspaceLocation.resolve(subPath);
        }
        Path parentFolder = workspaceChildPath.getParent();
        if (!Files.exists(parentFolder)) {
            try {
                Files.createDirectories(parentFolder);
            } catch (IOException e) {
                throw new IllegalStateException("Was not able to create parent structure for sub path:" + subPath);
            }
        }
        Path parentRealPath;
        try {
            parentRealPath = parentFolder.toRealPath();
        } catch (IOException e) {
            throw new IllegalStateException("Was not able to resolve real parent path for parent folder:" + parentFolder);
        }
        Path childPath = parentRealPath.resolve(workspaceChildPath.getFileName());
        return childPath;
    }

    private boolean isDirectoryEmptyOrDoesNotExist(Path extractedSourcesLocation) {
        try {
            if (!Files.exists(extractedSourcesLocation)) {
                return true;
            }
            return PathUtils.isEmptyDirectory(extractedSourcesLocation);
        } catch (IOException e) {
            throw new IllegalStateException("Was not able to detect if directory is empty or not:" + extractedSourcesLocation, e);
        }
    }

    private static abstract class ArchiveFilter implements FileFilter {

        @Override
        public final boolean accept(File pathname) {
            return pathname.getName().endsWith(getArchiveEnding());
        }

        protected abstract ArchiveType getArchiveType();

        protected final String getArchiveEnding() {
            return "." + getArchiveType().name().toLowerCase();
        }
    }

    private static class TarFileFilter extends ArchiveFilter {

        @Override
        protected ArchiveType getArchiveType() {
            return ArchiveType.TAR;
        }

    }

    private static class SourcecodeZipFileFilter extends ArchiveFilter {

        @Override
        protected ArchiveType getArchiveType() {
            return ArchiveType.ZIP;
        }
    }

    private class PreparationContext {
        private boolean binaryAccepted;
        private boolean sourceAccepted;

        public boolean isSourceAccepted() {
            return sourceAccepted;
        }

        public boolean isBinaryAccepted() {
            return binaryAccepted;
        }
    }

}
