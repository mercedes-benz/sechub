// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.PDSMustBeDocumented;
import com.daimler.sechub.pds.PDSNotFoundException;
import com.daimler.sechub.pds.config.PDSProductSetup;
import com.daimler.sechub.pds.config.PDSServerConfigurationService;
import com.daimler.sechub.pds.storage.PDSMultiStorageService;
import com.daimler.sechub.pds.storage.PDSStorageInfoCollector;
import com.daimler.sechub.pds.util.PDSFileUnzipSupport;
import com.daimler.sechub.pds.util.PDSFileUnzipSupport.UnzipResult;
import com.daimler.sechub.storage.core.JobStorage;

@Service
public class PDSWorkspaceService {

    public static final String OUTPUT = "output";
    public static final String RESULT_TXT = "result.txt";
    public static final String SYSTEM_OUT_LOG = "system-out.log";
    public static final String SYSTEM_ERROR_LOG = "system-error.log";

    private static final String UPLOAD = "upload";

    private static final Logger LOG = LoggerFactory.getLogger(PDSWorkspaceService.class);
    private static final String WORKSPACE_PARENT_FOLDER_PATH = "./";

    @PDSMustBeDocumented(value = "Set pds workspace root folder path. Inside this path the sub directory `workspace` will be created.", scope = "execution")
    @Value("${sechub.pds.workspace.rootfolder:" + WORKSPACE_PARENT_FOLDER_PATH + "}")
    String uploadBasePath = WORKSPACE_PARENT_FOLDER_PATH;

    @Autowired
    PDSMultiStorageService storageService;

    @Autowired
    PDSServerConfigurationService serverConfigService;

    @Autowired
    PDSFileUnzipSupport fileUnzipSupport;
    
    @Autowired
    PDSStorageInfoCollector storageInfoCollector;

    @PDSMustBeDocumented(value = "Defines if workspace is automatically cleaned when no longer necessary - means launcher script has been executed and finished (failed or done)", scope = "execution")
    @Value("${sechub.pds.workspace.autoclean.disabled:false}")
    private boolean workspaceAutoCleanDisabled;

    /**
     * Prepares workspace:
     * <ol>
     * <li><Fetch data from storage and copy to local workspace</li>
     * </ol>
     * 
     * @param config
     */
    public void prepareWorkspace(UUID jobUUID, PDSJobConfiguration config) throws IOException {
        File jobFolder = getUploadFolder(jobUUID);

        JobStorage storage = fetchStorage(jobUUID, config);
        Set<String> names = storage.listNames();

        LOG.debug("For jobUUID={} following names are found in storage:{}", jobUUID, names);

        for (String name : names) {

            InputStream fetchedInputStream = storage.fetch(name);
            File uploadFile = new File(jobFolder, name);

            try {
                FileUtils.copyInputStreamToFile(fetchedInputStream, uploadFile);
                LOG.debug("Imported file {} for job {} from storage to {}", name, jobUUID, uploadFile.getAbsolutePath());
            } catch (IOException e) {
                LOG.error("Was not able to import {} for job {}, reason:", name, jobUUID, e.getMessage());
                throw new IllegalArgumentException("Cannot import given file from storage", e);
            }

        }

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
        Path jobWorkspacePath = Paths.get(uploadBasePath, "workspace", jobUUID.toString());
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

    public void unzipUploadsWhenConfigured(UUID jobUUID, PDSJobConfiguration config) throws IOException {
        PDSProductSetup product = serverConfigService.getProductSetupOrNull(config.getProductId());
        if (product == null) {
            throw new PDSNotFoundException("Product referenced inside Job configuration does not exist at this server, id=" + config.getProductId());
        }
        if (!product.isUnzipUploads()) {
            return;
        }
        unzipUploads(jobUUID, true);
    }

    void unzipUploads(UUID jobUUID, boolean deleteOriginZipFiles) throws IOException {
        File uploadFolder = getUploadFolder(jobUUID);
        File[] zipFiles = uploadFolder.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".zip");
            }
        });
        int amountOfFiles = zipFiles.length;
        LOG.debug("{} zip file(s) found for job {}", amountOfFiles, jobUUID);
        if (amountOfFiles == 0) {
            return;
        }
        File unzipFolder = new File(uploadFolder, "unzipped");
        for (File zipFile : zipFiles) {

            File destDir = new File(unzipFolder, FilenameUtils.getBaseName(zipFile.getName()));
            UnzipResult unzipResult = fileUnzipSupport.unzipArchive(zipFile, destDir);

            LOG.info("Unzipped {} files to {}", unzipResult.getExtractedFilesCount(), unzipResult.getTargetLocation());

            if (deleteOriginZipFiles) {
                LOG.debug("Forcing delete of origin zip file {} ", zipFile);
                FileUtils.forceDelete(zipFile);
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

    public long getMinutesToWaitForResult(PDSJobConfiguration config) {
        PDSProductSetup productSetup = serverConfigService.getProductSetupOrNull(config.getProductId());
        if (productSetup == null) {
            return -1;
        }
        return productSetup.getMinutesToWaitForProductResult();
    }

    public String getFileEncoding(UUID jobUUID) {
        return "UTF-8"; // currently only UTF-8 expected
    }

    public WorkspaceLocationData createLocationData(UUID jobUUID) {
        File workspaceFolder = getWorkspaceFolder(jobUUID);
        Path workspaceFolderPath = workspaceFolder.toPath();
        WorkspaceLocationData locationData = new WorkspaceLocationData();

        try {

            locationData.workspaceLocation = createWorkspacePath(workspaceFolderPath, null);
            locationData.resultFileLocation = createWorkspacePath(workspaceFolderPath, OUTPUT + File.separator + RESULT_TXT);
            locationData.unzippedSourceLocation = createWorkspacePath(workspaceFolderPath, "upload/unzipped/sourcecode");
            locationData.zippedSourceLocation = createWorkspacePath(workspaceFolderPath, "upload/sourcecode.zip");

        } catch (IOException e) {
            throw new IllegalStateException("Was not able to create pathes");
        }

        return locationData;
    }

    private String createWorkspacePath(Path workspaceLocation, String subPath) throws IOException {
        Path workspaceChildPath;
        if (subPath == null) {
            workspaceChildPath = workspaceLocation;
        } else {
            workspaceChildPath = workspaceLocation.resolve(subPath);
        }
        Path parentFolder = workspaceChildPath.getParent();
        if (!Files.exists(parentFolder)) {
            Files.createDirectories(parentFolder);
        }
        Path parentRealPath = parentFolder.toRealPath();
        Path childPath = parentRealPath.resolve(workspaceChildPath.getFileName());
        String result = childPath.toString();
        return result;
    }

}
