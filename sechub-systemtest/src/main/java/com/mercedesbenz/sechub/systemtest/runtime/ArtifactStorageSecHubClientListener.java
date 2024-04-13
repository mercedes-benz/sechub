// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.systemtest.runtime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import org.apache.commons.io.file.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.api.SecHubClientListener;
import com.mercedesbenz.sechub.api.SecHubReport;
import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport.ArchivesCreationResult;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;

/**
 * This class stores files inside the runtime artifacts folder of the current
 * test when client informs about dedicated actions.
 *
 * @author Albert Tregnaghi
 *
 */
class ArtifactStorageSecHubClientListener implements SecHubClientListener {

    private static final Logger LOG = LoggerFactory.getLogger(ArtifactStorageSecHubClientListener.class);

    private SystemTestRuntimeContext context;
    private TextFileWriter textFileWriter;

    public ArtifactStorageSecHubClientListener(SystemTestRuntimeContext context) {
        this.context = context;
        this.textFileWriter = new TextFileWriter();
    }

    @Override
    public void beforeUpload(UUID secHubJobUUID, SecHubConfigurationModel model, ArchivesCreationResult archiveCreationResult) {
        Path targetFolder = ensureArtifactsFolderForTest();

        storeSecHubJobUUIDFile(secHubJobUUID, targetFolder);

        copySourceCodeZipFile(archiveCreationResult, targetFolder);
        copyBinaryArchiveFile(archiveCreationResult, targetFolder);
    }

    private Path ensureArtifactsFolderForTest() {
        SystemTestRunResult currentResult = context.getCurrentResult();
        if (currentResult == null) {
            throw new IllegalStateException("current result may not be null!");
        }
        String testName = currentResult.getTestName();
        if (testName == null) {
            throw new IllegalStateException("Current result testname must not be null!");
        }
        Path targetFolder = context.getLocationSupport().ensureRuntimeArtifactsFolderRealPath(testName);
        return targetFolder;
    }

    @Override
    public void afterReportDownload(UUID jobUUID, SecHubReport report) {
        Path targetFolder = ensureArtifactsFolderForTest();

        String prettyPrintedJson = JSONConverter.get().toJSON(report, true);

        File targetFile = new File(targetFolder.toFile(), "sechub-report.json");
        try {
            textFileWriter.save(targetFile, prettyPrintedJson, true);
        } catch (IOException e) {
            LOG.error("Was not able to store sechub config file: {}", targetFile, e);
        }
    }

    private void copySourceCodeZipFile(ArchivesCreationResult archiveCreationResult, Path targetFolder) {
        if (!archiveCreationResult.isSourceArchiveCreated()) {
            return;
        }
        copySourceFileToTargetFolder(archiveCreationResult.getSourceArchiveFile(), targetFolder);
    }

    private void copyBinaryArchiveFile(ArchivesCreationResult archiveCreationResult, Path targetFolder) {
        if (!archiveCreationResult.isBinaryArchiveCreated()) {
            return;
        }
        copySourceFileToTargetFolder(archiveCreationResult.getBinaryArchiveFile(), targetFolder);
    }

    private void copySourceFileToTargetFolder(Path sourceFile, Path targetFolder) {
        try {
            PathUtils.copyFileToDirectory(sourceFile, targetFolder);
        } catch (IOException e) {
            LOG.error("Was not able to copy file: {} to target folder: {}", sourceFile, targetFolder, e);
        }
    }

    private void storeSecHubJobUUIDFile(UUID secHubJobUUID, Path targetFolder) {
        File targetFile = new File(targetFolder.toFile(), "sechub-job-uuid.txt");
        try {
            textFileWriter.save(targetFile, secHubJobUUID.toString(), true);
        } catch (IOException e) {
            LOG.error("Was not able to store sechub job uuid file: {}", targetFile, e);
        }

    }

}