// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.checkmarx.scan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.mercedesbenz.sechub.adapter.mock.MockDataIdentifierFactory;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport.ArchiveType;
import com.mercedesbenz.sechub.commons.core.CommonConstants;
import com.mercedesbenz.sechub.commons.mapping.NamePatternIdProvider;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;
import com.mercedesbenz.sechub.commons.pds.PDSUserMessageSupport;
import com.mercedesbenz.sechub.wrapper.checkmarx.cli.CheckmarxWrapperEnvironment;

public class CheckmarxWrapperScanContext {

    SecHubConfigurationModel configuration;
    CheckmarxWrapperEnvironment environment;
    NamePatternIdProvider presetIdProvider;
    NamePatternIdProvider teamIdProvider;
    ArchiveSupport archiveSupport;
    MockDataIdentifierFactory mockDataIdentifierFactory;
    PDSUserMessageSupport messageSupport;

    CheckmarxWrapperScanContext() {
    }

    public String createMockDataIdentifier() {
        return mockDataIdentifierFactory.createMockDataIdentifier(ScanType.CODE_SCAN, configuration);
    }

    public InputStream createSourceCodeZipFileInputStream() throws IOException {
        String folderAsString = environment.getPdsJobExtractedSourceFolder();
        if (folderAsString == null) {
            throw new IllegalStateException("The folder for the extracted sources is not defined!");
        }
        File extractedSourcesFolder = new File(folderAsString);
        assertSourcesFound(extractedSourcesFolder);

        File sourceCodeZipFile = createSourceCodeZipFileForExtractedSources(extractedSourcesFolder);

        return new FileInputStream(sourceCodeZipFile);
    }

    public CheckmarxWrapperEnvironment getEnvironment() {
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

    private void assertSourcesFound(File extractedSourcesFolder) throws IOException, FileNotFoundException {
        if (!extractedSourcesFolder.exists()) {
            writeUserMessageAboutMissingSources();
            throw new FileNotFoundException("The folder does not exist:" + extractedSourcesFolder);
        }
        File[] files = extractedSourcesFolder.listFiles();
        if (files == null) {
            writeUserMessageAboutMissingSources();
            throw new IllegalStateException("Source folder cannot list files:" + extractedSourcesFolder);
        }
        if (files.length == 0) {
            writeUserMessageAboutMissingSources();
            throw new IllegalStateException("No sources found to scan!");
        }
    }

    private void writeUserMessageAboutMissingSources() throws IOException {
        messageSupport.writeMessage(new SecHubMessage(SecHubMessageType.ERROR,
                "There were no sources for code scan available. " + "Maybe your SecHub configuration file is misconfigured, or you did not upload sources."));
    }

    private File createSourceCodeZipFileForExtractedSources(File extractedSourcesFolder) throws IOException {
        File parentFolder = extractedSourcesFolder.getParentFile();
        File recompressedFolder = new File(parentFolder, "recompressed");
        File targetArchiveFile = new File(recompressedFolder, CommonConstants.FILENAME_SOURCECODE_ZIP);

        archiveSupport.compressFolder(ArchiveType.ZIP, extractedSourcesFolder, targetArchiveFile);

        return targetArchiveFile;
    }

}
