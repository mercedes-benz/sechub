package com.mercedesbenz.sechub.systemtest.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import com.mercedesbenz.sechub.commons.archive.ArchiveConstants;
import com.mercedesbenz.sechub.commons.core.CommonConstants;
import com.mercedesbenz.sechub.commons.core.IgnoreOutputHandler;
import com.mercedesbenz.sechub.commons.core.OutputHandler;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationObject;
import com.mercedesbenz.sechub.commons.model.SecHubFileSystemConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubFileSystemContainer;

public class SystemTestUploadFilesSupport {

    private boolean createPseudoFilesWhenNotExisting = true;
    private File targetFolder;

    private OutputHandler outputHandler;

    private SystemTestArchiveSupport archiveSupport = new SystemTestArchiveSupport();
    private File sourceFolder;

    public SystemTestUploadFilesSupport(File sourceFolder, File targetFolder) {
        this.sourceFolder = sourceFolder;
        this.targetFolder = targetFolder;
    }

    private OutputHandler getOutputHandler() {
        if (outputHandler == null) {
            outputHandler = new IgnoreOutputHandler();
        }
        return outputHandler;
    }

    public void setOutputHandler(OutputHandler outputHandler) {
        this.outputHandler = outputHandler;
    }

    public void setCreatePseudoFilesWhenNotExisting(boolean create) {
        this.createPseudoFilesWhenNotExisting = create;
    }

    public void createUploadFilesAsConfiguredInSecHubConfigFile(SecHubConfigurationModel config, ScanType scanType) throws IOException {

        File extracted = new File(targetFolder, "extracted");

        File binaryCopy = new File(extracted, "binaries");
        File sourceCopy = new File(extracted, "source");

        copyOriginFilesToExtractedFolder(config, binaryCopy, sourceCopy);

        archiveSupport.compressToTar(binaryCopy, new File(targetFolder, CommonConstants.FILENAME_BINARIES_TAR));
        archiveSupport.compressToZip(sourceCopy, new File(targetFolder, CommonConstants.FILENAME_SOURCECODE_ZIP));

    }

    private void copyOriginFilesToExtractedFolder(SecHubConfigurationModel config, File binaryCopy, File sourceCopy) throws FileNotFoundException, IOException {
        binaryCopy.mkdirs();
        sourceCopy.mkdirs();

        /* copy data parts */
        if (config.getData().isPresent()) {
            SecHubDataConfiguration data = config.getData().get();

            copy(binaryCopy, data.getBinaries());

            copy(sourceCopy, data.getSources());
        }

        /* copy embedded parts */
        Optional<SecHubCodeScanConfiguration> codeScanOpt = config.getCodeScan();
        if (codeScanOpt.isPresent()) {
            SecHubCodeScanConfiguration codeScan = codeScanOpt.get();
            copy(sourceCopy, Arrays.asList(codeScan));
        }
    }

    private void copy(File binaryCopy, List<? extends SecHubFileSystemContainer> fileSystemContainers) throws FileNotFoundException, IOException {
        for (SecHubFileSystemContainer config : fileSystemContainers) {
            Optional<SecHubFileSystemConfiguration> fileSystemOpt = config.getFileSystem();
            if (!fileSystemOpt.isPresent()) {
                continue;
            }
            File baseTarget = null;
            if ((config instanceof SecHubDataConfigurationObject)) {
                SecHubDataConfigurationObject dataConfigObject = (SecHubDataConfigurationObject) config;
                String uniqueName = dataConfigObject.getUniqueName();
                baseTarget = new File(binaryCopy, ArchiveConstants.DATA_SECTION_FOLDER + uniqueName);
            } else {
                baseTarget = binaryCopy; // root folder, no data section
            }

            SecHubFileSystemConfiguration fileSystem = fileSystemOpt.get();
            copyFileOrFolder(baseTarget, fileSystem.getFiles(), false);
            copyFileOrFolder(baseTarget, fileSystem.getFolders(), true);

        }
    }

    private void copyFileOrFolder(File baseTarget, List<String> files, boolean targetIsDirectory) throws FileNotFoundException, IOException {
        for (String fileName : files) {
            File file = new File(sourceFolder, fileName);
            boolean notExisting = !file.exists();

            File targetFile = new File(baseTarget, fileName);
            if (targetIsDirectory) {
                targetFile.mkdirs();
            } else {
                targetFile.getParentFile().mkdirs();
            }
            if (notExisting) {
                if (createPseudoFilesWhenNotExisting) {
                    if (targetIsDirectory) {
                        File pseudoFile = new File(targetFile, "gen_placeholder_because_origin_not_existing.txt");
                        pseudoFile.createNewFile();
                        getOutputHandler().warn("origin folder:" + file.getAbsolutePath() + " did not exist. So created folder at "
                                + targetFile.getAbsolutePath() + " with place holder file.");

                    } else {
                        targetFile.createNewFile();
                        getOutputHandler()
                                .warn("origin file:" + file.getAbsolutePath() + " did not exist. So created pseudo file:" + targetFile.getAbsolutePath());
                    }

                    /*
                     * We cannot create but have created pseudo-files/empty directories, so stop
                     * here
                     */
                    continue;
                } else {
                    throw new FileNotFoundException("Cannot copy file/folder because it does not exist:" + file.getAbsolutePath());
                }

            }
            if (targetIsDirectory) {
                FileUtils.copyDirectory(file, targetFile);
            } else {
                FileUtils.copyFile(file, targetFile);
            }
        }
    }

}
