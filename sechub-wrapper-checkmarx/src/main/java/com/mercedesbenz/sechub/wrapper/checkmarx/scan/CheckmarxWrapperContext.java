package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport.ArchiveType;
import com.mercedesbenz.sechub.commons.core.CommonConstants;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProvider;
import com.mercedesbenz.sechub.commons.model.CodeScanPathCollector;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperCLIEnvironment;

public class CheckmarxWrapperContext {

    SecHubConfigurationModel configuration;
    CheckmarxWrapperCLIEnvironment environment;
    NamePatternIdProvider presetIdProvider;
    NamePatternIdProvider teamIdProvider;
    ArchiveSupport archiveSupport;
    CodeScanPathCollector codeScanPathCollector;

    CheckmarxWrapperContext() {
    }

    public Set<String> calculateCodeUploadFileSystemFolders() {
        if (configuration == null) {
            throw new IllegalStateException("configuration model may not be null!");
        }
        return codeScanPathCollector.collectAllCodeScanPathes(configuration);
    }

    public InputStream createSourceCodeZipFileInputStream() throws IOException {
        String folderAsString = environment.getPdsJobExtractedSourceFolder();
        if (folderAsString == null) {
            throw new IllegalStateException("The folder for the extracted sources is not defined!");
        }
        File extractedSourcesFolder = new File(folderAsString);
        if (!extractedSourcesFolder.exists()) {
            throw new FileNotFoundException("The folder does not exist:" + folderAsString);
        }
        File sourceCodeZipFile = createSourceCodeZipFileForExtractedSources(extractedSourcesFolder);

        return new FileInputStream(sourceCodeZipFile);
    }

    protected File createSourceCodeZipFileForExtractedSources(File extractedSourcesFolder) throws IOException {
        File parentFolder = extractedSourcesFolder.getParentFile();
        File recompressedFolder = new File(parentFolder, "recompressed");
        File targetArchiveFile = new File(recompressedFolder, CommonConstants.FILENAME_SOURCECODE_ZIP);

        archiveSupport.compressFolder(ArchiveType.ZIP, extractedSourcesFolder, targetArchiveFile);

        return targetArchiveFile;
    }

    public CheckmarxWrapperCLIEnvironment getEnvironment() {
        return environment;
    }

    public SecHubConfigurationModel getConfiguration() {
        return configuration;
    }

    public String getTeamIdForNewProjects() {
        String projectId = getProjectId();
        String teamId = teamIdProvider.getIdForName(projectId);
        if (teamId == null) {
            throw new IllegalStateException("Was not able to determine the team id for project: " + projectId);
        }
        return teamId;
    }

    public Long getPresetIdForNewProjects() {
        String projectId = getProjectId();

        String presetId = presetIdProvider.getIdForName(projectId);
        if (presetId == null) {
            throw new IllegalStateException("Was not able to determine the preset id for project: " + projectId);
        }
        return Long.valueOf(presetId);
    }

    public String getProjectId() {
        String projectId = configuration.getProjectId();
        if (projectId == null) {
            throw new IllegalStateException("Project id is missing!");
        }
        return projectId;
    }
}
