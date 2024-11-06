// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static com.mercedesbenz.sechub.commons.core.CommonConstants.FILENAME_BINARIES_TAR;
import static com.mercedesbenz.sechub.commons.core.CommonConstants.FILENAME_SOURCECODE_ZIP;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.archive.ArchiveExtractionConstraints;
import com.mercedesbenz.sechub.commons.archive.ArchiveExtractionResult;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport.ArchiveType;
import com.mercedesbenz.sechub.commons.archive.FileSize;
import com.mercedesbenz.sechub.commons.archive.SecHubFileStructureDataProvider;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.pds.execution.ExecutionEventData;
import com.mercedesbenz.sechub.commons.pds.execution.ExecutionEventDetailIdentifier;
import com.mercedesbenz.sechub.commons.pds.execution.ExecutionEventType;
import com.mercedesbenz.sechub.pds.PDSMustBeDocumented;
import com.mercedesbenz.sechub.pds.PDSNotFoundException;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSProductSetup;
import com.mercedesbenz.sechub.pds.config.PDSServerConfigurationService;
import com.mercedesbenz.sechub.pds.storage.PDSMultiStorageService;
import com.mercedesbenz.sechub.pds.storage.PDSStorageInfoCollector;
import com.mercedesbenz.sechub.pds.util.PDSArchiveSupportProvider;
import com.mercedesbenz.sechub.pds.util.PDSResilientRetryExecutor;
import com.mercedesbenz.sechub.pds.util.PDSResilientRetryExecutor.ExceptionThrower;
import com.mercedesbenz.sechub.storage.core.JobStorage;

@Service
public class PDSWorkspaceService {

    public static final String UPLOAD = "upload";
    public static final String EXTRACTED = "extracted";
    public static final String EXTRACTED_SOURCES = EXTRACTED + "/sources";
    public static final String EXTRACTED_BINARIES = EXTRACTED + "/binaries";

    public static final String OUTPUT = "output";
    public static final String MESSAGES = "messages";
    public static final String EVENTS = "events";
    public static final String RESULT_TXT = "result.txt";
    public static final String METADATA_TXT = "metadata.txt";

    public static final String SYSTEM_OUT_LOG = "system-out.log";
    public static final String SYSTEM_ERROR_LOG = "system-error.log";

    private static final Logger LOG = LoggerFactory.getLogger(PDSWorkspaceService.class);
    private static final String DEFAULT_WORKSPACE_ROOTFOLDER_PATH = "./workspace/";

    @PDSMustBeDocumented(value = "Set PDS workspace root folder path. Each running PDS job will have its own temporary sub directory inside this folder. ", scope = "execution")
    @Value("${pds.workspace.rootfolder:" + DEFAULT_WORKSPACE_ROOTFOLDER_PATH + "}")
    String workspaceRootFolderPath = DEFAULT_WORKSPACE_ROOTFOLDER_PATH;

    @PDSMustBeDocumented(value = "Defines if workspace is automatically cleaned when no longer necessary - means launcher script has been executed and finished (failed or done). This is useful for debugging, but should not be used in production.", scope = "execution")
    @Value("${pds.workspace.autoclean.disabled:false}")
    private boolean workspaceAutoCleanDisabled;

    @PDSMustBeDocumented(value = "Defines the max file size of the uncompressed archive (e.g.: 10KB or 10mb)", scope = "execution")
    @Value("${pds.archive.extraction.max-file-size-uncompressed}")
    private FileSize archiveExtractionMaxFileSizeUncompressed;

    @PDSMustBeDocumented(value = "Defines how many entries the archive may have", scope = "execution")
    @Value("${pds.archive.extraction.max-entries}")
    private long archiveExtractionMaxEntries;

    @PDSMustBeDocumented(value = "Defines the maximum directory depth for an entry inside the archive", scope = "execution")
    @Value("${pds.archive.extraction.max-directory-depth}")
    private long archiveExtractionMaxDirectoryDepth;

    @PDSMustBeDocumented(value = "Defines the timeout of the archive extraction process", scope = "execution")
    @Value("${pds.archive.extraction.timeout}")
    private Duration archiveExtractionTimeout;

    @Autowired
    PDSMultiStorageService storageService;

    @Autowired
    PDSServerConfigurationService serverConfigService;

    @Autowired
    PDSArchiveSupportProvider archiveSupportProvider;

    @Autowired
    PDSWorkspacePreparationContextFactory preparationContextFactory;

    @Autowired
    PDSStorageInfoCollector storageInfoCollector;

    @Autowired
    TextFileWriter textFileWriter;

    @Autowired
    TextFileReader textFileReader;

    @Autowired
    PDSWorkspacePreparationResultCalculator preparationResultCalculator;

    private static final ArchiveFilter TAR_FILE_FILTER = new TarFileFilter();

    private static final ArchiveFilter ZIP_FILE_FILTER = new SourcecodeZipFileFilter();

    private final ExceptionThrower<IOException> readStorageExceptionThrower;

    public PDSWorkspaceService() {
        readStorageExceptionThrower = new ExceptionThrower<IOException>() {

            @Override
            public void throwException(String message, Exception cause) throws IOException {
                throw new IOException("Storage read failed. " + message, cause);
            }
        };
    }

    /**
     * Prepares workspace:
     * <ol>
     * <li>Creates preparation context depending on job configuration</li>
     * <li>Fetch data from storage and copy to local workspace for wanted parts</li>
     * <li>Extract data</li>
     * <li>Calculate preparation and return preparation result</li>
     * </ol>
     *
     * @param pdsJobUUID
     * @param configuration
     * @param metaData
     * @return {@link PDSWorkspacePreparationResult}, never <code>null</code>
     * @throws IOException
     */
    public PDSWorkspacePreparationResult prepare(UUID pdsJobUUID, PDSJobConfiguration config, String metaData) throws IOException {

        PDSJobConfigurationSupport configurationSupport = new PDSJobConfigurationSupport(config);

        PDSWorkspacePreparationContext preparationContext = preparationContextFactory.createPreparationContext(configurationSupport);

        LOG.debug("Info about workspace for PDS job: {}. Sources accepted: {}. Binaries accepted: {}", pdsJobUUID, preparationContext.isSourceAccepted(),
                preparationContext.isBinaryAccepted());

        writeMetaData(pdsJobUUID, metaData);

        importWantedFilesFromJobStorage(pdsJobUUID, config, configurationSupport, preparationContext);

        extractZipFileUploadsWhenConfigured(pdsJobUUID, config, preparationContext);
        extractTarFileUploadsWhenConfigured(pdsJobUUID, config, preparationContext);

        return preparationResultCalculator.calculateResult(preparationContext);
    }

    private void writeMetaData(UUID jobUUID, String metaData) throws IOException {

        if (metaData != null && !metaData.isEmpty()) {
            File metaDataFile = getMetaDataFile(jobUUID);
            LOG.debug("Meta data found for PDS job {} - will create metadata file {}", jobUUID, metaDataFile);

            TextFileWriter writer = new TextFileWriter();
            writer.writeTextToFile(metaDataFile, metaData, true);
            LOG.info("Created meta data file for PDS job {}", jobUUID);
        }
    }

    private void importWantedFilesFromJobStorage(UUID pdsJobUUID, PDSJobConfiguration config, PDSJobConfigurationSupport configurationSupport,
            PDSWorkspacePreparationContext preparationContext) throws IOException {

        PDSResilientRetryExecutor<IOException> resilientStorageReadExecutor = createResilientReadExecutor(preparationContext);

        File jobFolder = getUploadFolder(pdsJobUUID);
        JobStorage storage = fetchStorage(pdsJobUUID, config);

        Set<String> names = resilientStorageReadExecutor.execute(() -> storage.listNames(), "List storage names for job: " + pdsJobUUID.toString());

        LOG.debug("For pds jobUUID: {} following names are found in storage: {}", pdsJobUUID, names);

        try {
            for (String name : names) {

                if (isWantedStorageContent(name, configurationSupport, preparationContext)) {
                    resilientStorageReadExecutor.execute(() -> readAndCopyStorageToFileSystem(pdsJobUUID, jobFolder, storage, name),
                            "Read and copy storage: " + name + " for job: " + pdsJobUUID);

                } else {
                    LOG.debug("Did NOT import '{}' for job {} from storage - was not wanted", name, pdsJobUUID);
                }

            }
        } finally {
            storage.close();
        }
    }

    private PDSResilientRetryExecutor<IOException> createResilientReadExecutor(PDSWorkspacePreparationContext preparationContext) {
        PDSResilientRetryExecutor<IOException> resilientExecutor = new PDSResilientRetryExecutor<>(preparationContext.getJobStorageReadResilienceRetriesMax(),
                readStorageExceptionThrower, IOException.class);

        resilientExecutor.setMilliSecondsToWaiBeforeRetry(preparationContext.getJobStorageReadResilienceRetryWaitSeconds() * 1000);

        return resilientExecutor;
    }

    private void readAndCopyStorageToFileSystem(UUID jobUUID, File jobFolder, JobStorage storage, String name) throws IOException {

        File uploadFile = new File(jobFolder, name);

        try (InputStream fetchedInputStream = storage.fetch(name)) {

            try {

                FileUtils.copyInputStreamToFile(fetchedInputStream, uploadFile);

                LOG.debug("Imported '{}' for job {} from storage to {}", name, jobUUID, uploadFile.getAbsolutePath());

            } catch (IOException e) {

                LOG.error("Was not able to copy stream of uploaded file: {} for job {}, reason: ", name, jobUUID, e.getMessage());

                if (uploadFile.exists()) {
                    boolean deleteSuccessful = uploadFile.delete();
                    LOG.info("Uploaded file existed. Deleted successfully: {}", deleteSuccessful);
                }
                throw e;
            }
        }

    }

    void extractZipFileUploadsWhenConfigured(UUID jobUUID, PDSJobConfiguration config, PDSWorkspacePreparationContext preparationContext) throws IOException {
        if (!preparationContext.isSourceAccepted()) {
            return;
        }
        PDSProductSetup productSetup = resolveProductSetup(config);

        ScanType scanType = productSetup.getScanType();
        SecHubFileStructureDataProvider provider = resolveFileStructureDataProviderOrNull(jobUUID, config, scanType);
        preparationContext.setExtractedSourceAvailable(extractUploadedZipFiles(jobUUID, true, provider));
    }

    void extractTarFileUploadsWhenConfigured(UUID jobUUID, PDSJobConfiguration config, PDSWorkspacePreparationContext preparationContext) throws IOException {
        if (!preparationContext.isBinaryAccepted()) {
            return;
        }
        PDSProductSetup productSetup = resolveProductSetup(config);

        ScanType scanType = productSetup.getScanType();
        SecHubFileStructureDataProvider provider = resolveFileStructureDataProviderOrNull(jobUUID, config, scanType);
        preparationContext.setExtractedBinaryAvailable(extractUploadedTarFiles(jobUUID, true, provider));
    }

    private boolean isWantedStorageContent(String name, PDSJobConfigurationSupport configurationSupport, PDSWorkspacePreparationContext preparationContext) {
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

        UUID pdsOrSecHubJobUUID;
        String storagePath;
        PDSJobConfigurationSupport configurationSupport = new PDSJobConfigurationSupport(config);

        boolean useSecHubStorage = configurationSupport.isSecHubStorageEnabled();

        if (useSecHubStorage) {
            storagePath = configurationSupport.getSecHubStoragePath();
            pdsOrSecHubJobUUID = config.getSechubJobUUID();
        } else {
            storagePath = null;// will force default storage path for the PDS product
            pdsOrSecHubJobUUID = pdsJobUUID;
        }

        LOG.debug("PDS job {}: feching storage for storagePath={}, {}-jobUUID={}, useSecHubStorage={}", pdsJobUUID, storagePath,
                useSecHubStorage ? "sechub" : "pds", pdsOrSecHubJobUUID, useSecHubStorage);
        JobStorage storage = storageService.createJobStorageForPath(storagePath, pdsOrSecHubJobUUID);

        storageInfoCollector.informFetchedStorage(storagePath, config.getSechubJobUUID(), pdsJobUUID, storage);

        return storage;
    }

    /**
     * Resolves upload folder - if not existing it will be created
     *
     * @param pdsJobUUID
     * @return upload folder
     */
    public File getUploadFolder(UUID pdsJobUUID) {
        File file = new File(getWorkspaceFolder(pdsJobUUID), UPLOAD);
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
     * @param pdsJobUUID
     * @return upload folder
     * @throws IllegalStateException in case the workspace folder does not exist and
     *                               cannot be created (e.g. because of missing
     *                               permissions)
     */
    public File getWorkspaceFolder(UUID pdsJobUUID) {
        Path jobWorkspacePath = Paths.get(workspaceRootFolderPath, pdsJobUUID.toString());
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

    SecHubFileStructureDataProvider resolveFileStructureDataProviderOrNull(UUID pdsJobUUID, PDSJobConfiguration config, ScanType scanType) throws IOException {

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

    private boolean extractUploadedZipFiles(UUID jobUUID, boolean deleteOriginFiles, SecHubFileStructureDataProvider configuration) throws IOException {
        return extractArchives(jobUUID, deleteOriginFiles, configuration, ZIP_FILE_FILTER, EXTRACTED_SOURCES);

    }

    private boolean extractUploadedTarFiles(UUID jobUUID, boolean deleteOriginFiles, SecHubFileStructureDataProvider configuration) throws IOException {
        return extractArchives(jobUUID, deleteOriginFiles, configuration, TAR_FILE_FILTER, EXTRACTED_BINARIES);

    }

    private boolean extractArchives(UUID pdsJobUUID, boolean deleteOriginFiles, SecHubFileStructureDataProvider configuration, ArchiveFilter fileFilter,
            String extractionSubfolder) throws IOException {

        File uploadFolder = getUploadFolder(pdsJobUUID);
        File[] archiveFiles = uploadFolder.listFiles(fileFilter);

        int amountOfFiles = archiveFiles.length;
        LOG.debug("{} *{} file(s) found for job {}", amountOfFiles, fileFilter.getArchiveEnding(), pdsJobUUID);
        if (amountOfFiles == 0) {
            LOG.info("No files found to extract into {} for {} - before filtering.", extractionSubfolder, pdsJobUUID);
            return false;
        }

        ArchiveType archiveType = fileFilter.getArchiveType();

        File extractionTargetFolder = new File(uploadFolder, extractionSubfolder);
        if (!extractionTargetFolder.mkdirs()) {
            throw new IOException("Was not able to create " + extractionTargetFolder.getAbsolutePath());
        }

        ArchiveSupport archiveSupport = archiveSupportProvider.getArchiveSupport();
        ArchiveExtractionConstraints archiveExtractionConstraints = new ArchiveExtractionConstraints(archiveExtractionMaxFileSizeUncompressed,
                archiveExtractionMaxEntries, archiveExtractionMaxDirectoryDepth, archiveExtractionTimeout);

        for (File archiveFile : archiveFiles) {
            try (FileInputStream archiveFileInputStream = new FileInputStream(archiveFile)) {

                ArchiveExtractionResult extractionResult = archiveSupport.extract(archiveType, archiveFileInputStream, archiveFile.getAbsolutePath(),
                        extractionTargetFolder, configuration, archiveExtractionConstraints);

                LOG.info("Extracted {} files to {}", extractionResult.getExtractedFilesCount(), extractionResult.getTargetLocation());

                if (deleteOriginFiles) {
                    LOG.debug("Forcing delete of origin file: {} ", archiveFile.getAbsolutePath());
                    FileUtils.forceDelete(archiveFile);
                }
            }
        }
        File[] extractedFiles = extractionTargetFolder.listFiles();
        if (extractedFiles == null || extractedFiles.length == 0) {
            LOG.info("No files found to extract into {} for {} - after filters have been applied.", extractionSubfolder, pdsJobUUID);
            return false;
        }
        return true;
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
            storage.close();
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
        locationData.eventsLocation = createEventFolderLocation(workspaceFolderPath).toString();
        locationData.metaDataFileLocation = createWorkspacePathAndEnsureParentDirectories(workspaceFolderPath, METADATA_TXT).toString();

        locationData.extractedSourcesLocation = createExtractedSourcesLocation(workspaceFolderPath).toString();
        locationData.extractedBinariesLocation = createExtractedBinariesLocation(workspaceFolderPath).toString();

        locationData.sourceCodeZipFileLocation = createSourceCodeZipFileLocation(workspaceFolderPath).toString();
        locationData.binariesTarFileLocation = createBinariesTarFileLocation(workspaceFolderPath).toString();

        return locationData;
    }

    /**
     * Sends the event into workspace - means a dedicated event file is written
     *
     * @param jobUUID   uuid of job
     * @param eventType event type
     */
    public void sendEvent(UUID jobUUID, ExecutionEventType eventType) {
        sendEvent(jobUUID, eventType, null);
    }

    /**
     * Sends the event into workspace - means a dedicated event file is written
     *
     * @param jobUUID
     * @param eventType
     * @param eventData
     */
    public void sendEvent(UUID jobUUID, ExecutionEventType eventType, ExecutionEventData eventData) {
        if (jobUUID == null) {
            throw new IllegalArgumentException("job uuid must be set!");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("event type be set!");
        }
        if (eventData == null) {
            eventData = new ExecutionEventData();
        }

        eventData.setDetail(ExecutionEventDetailIdentifier.EVENT_TYPE, eventType.getId());

        String eventJson = JSONConverter.get().toJSON(eventData);

        File eventFileToWrite = getEventFile(jobUUID, eventType);
        LOG.debug("Send event {} to workspace for sechub job: {}", eventType, jobUUID);

        try {
            textFileWriter.writeTextToFile(eventFileToWrite, eventJson, true);
        } catch (IOException e) {
            LOG.error("Was not able to send event: {} with text: '{}' to workspace for PDS job: {}", eventType, eventJson, jobUUID, e);
            throw new IllegalStateException("Execution event storage failed for job: " + jobUUID, e);

        }
    }

    /**
     * Fetch event data for an execution event.
     *
     * @param jobUUID
     * @param eventType
     * @return event data or <code>null</code> when the event file was not found
     * @throws IllegalStateException when event file cannot be read or contains
     *                               illegal JSON
     */
    public ExecutionEventData fetchEventDataOrNull(UUID jobUUID, ExecutionEventType eventType) {
        if (jobUUID == null) {
            throw new IllegalArgumentException("job UUID must be set!");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("event type must be set!");
        }
        File eventFileToRead = getEventFile(jobUUID, eventType);
        if (!eventFileToRead.exists()) {
            return null;
        }
        String json = null;
        try {
            json = textFileReader.readTextFromFile(eventFileToRead);
        } catch (IOException e) {
            LOG.error("Was not able to read event: {} from file: '{}' of PDS job: {}", eventType, eventFileToRead, jobUUID, e);
            throw new IllegalStateException("Execution event reading failed for job: " + jobUUID, e);
        }
        if (json.isEmpty()) {
            try {
                BasicFileAttributes fileAttrs = Files.readAttributes(eventFileToRead.toPath(), BasicFileAttributes.class);
                FileTime fileTime = fileAttrs.creationTime();

                ExecutionEventData fallback = new ExecutionEventData(fileTime.toInstant());
                fallback.setDetail(ExecutionEventDetailIdentifier.EVENT_TYPE, eventType.getId());
            } catch (IOException e) {
                LOG.error("Was not able to create missing data for empty json for event: {} from file: '{}' of PDS job: {}", eventType, eventFileToRead,
                        jobUUID, e);
                throw new IllegalStateException("Execution event reading fallback for empty json failed for job: " + jobUUID, e);
            }

        }
        try {
            ExecutionEventData eventData = JSONConverter.get().fromJSON(ExecutionEventData.class, json);
            return eventData;

        } catch (JSONConverterException e) {
            LOG.error("Was not able to convert event data for event: {} from file: '{}' to workspace for PDS job: {}", eventType, eventFileToRead, jobUUID, e);
            throw new IllegalStateException("Execution event reading failed for job: " + jobUUID, e);
        }

    }

    private File getEventFile(UUID jobUUID, ExecutionEventType event) {
        Path workspaceFolderPath = getWorkspaceFolderPath(jobUUID);
        Path eventFolder = createEventFolderLocation(workspaceFolderPath);
        File eventFileToWrite = new File(eventFolder.toFile(), event.name().toLowerCase() + ".json");
        return eventFileToWrite;
    }

    private Path createEventFolderLocation(Path workspaceFolderPath) {
        return createWorkspacePathAndEnsureParentDirectories(workspaceFolderPath, EVENTS);
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

}
