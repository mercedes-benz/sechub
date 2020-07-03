package com.daimler.sechub.pds.job;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.PDSNotFoundException;
import com.daimler.sechub.pds.config.PDSProductSetup;
import com.daimler.sechub.pds.config.PDSServerConfigurationService;
import com.daimler.sechub.pds.util.PDSFileUnzipSupport;

@Service
public class PDSWorkspaceService {
    private static final Logger LOG = LoggerFactory.getLogger(PDSWorkspaceService.class);
    private static final String WORKSPACE_PARENT_FOLDER_PATH = "./";

    @Value("${sechub.pds.workspace.rootfolder:" + WORKSPACE_PARENT_FOLDER_PATH + "}")
    String uploadBasePath = WORKSPACE_PARENT_FOLDER_PATH;

    @Autowired
    PDSServerConfigurationService serverConfigService;

    @Autowired
    PDSFileUnzipSupport fileUnzipSupport;

    /**
     * Resolves upload folder - if not existing it will be created
     * 
     * @param jobUUID
     * @return upload folder
     */
    public File getUploadFolder(UUID jobUUID) {
        File file = new File(getWorkspaceFolder(jobUUID),"upload");
        file.mkdirs();
        return file;
    }
    
    /**
     * Resolves upload folder - if not existing it will be created
     * 
     * @param jobUUID
     * @return upload folder
     */
    public File getWorkspaceFolder(UUID jobUUID) {
        Path p = Paths.get(uploadBasePath, "workspace", jobUUID.toString());
        File file = p.toFile();
        file.mkdirs();
        return file;
    }

    public void unzipUploadsWhenConfigured(UUID jobUUID, PDSJobConfiguration config) throws IOException {
        PDSProductSetup product = serverConfigService.getProductSetupOrNull(config.getProductId());
        if (product == null) {
            throw new PDSNotFoundException("Product referenced inside Job configuration does not exist at this server, id=" + config.getProductId());
        }
        if (!product.isUnzipUploads()) {
            return;
        }
        unzipUploads(jobUUID,true);
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
        LOG.debug("{} zip file(s) found for job {}",amountOfFiles, jobUUID);
        if (amountOfFiles == 0) {
            return;
        }
        File unzipFolder = new File(uploadFolder, "unzipped");
        for (File zipFile : zipFiles) {
            File destDir = new File(unzipFolder, FilenameUtils.getBaseName(zipFile.getName()));
            fileUnzipSupport.unzipArchive(zipFile, destDir);
            if (deleteOriginZipFiles) {
                FileUtils.forceDelete(zipFile);
            }
        }

    }

    public void cleanup(UUID jobUUID) throws IOException {
        FileUtils.deleteDirectory(getWorkspaceFolder(jobUUID));
    }

    public String getProductPathFor(PDSJobConfiguration config) {
        String productId = config.getProductId();
        PDSProductSetup productSetup = serverConfigService.getProductSetupOrNull(productId);
        if (productSetup==null) {
            return null;
        }
        return productSetup.getPath();
        
        
    }
}
