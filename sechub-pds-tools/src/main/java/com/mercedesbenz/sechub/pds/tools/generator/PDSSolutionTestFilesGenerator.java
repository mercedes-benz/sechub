// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.archive.ArchiveSupport;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelReducedCloningSupport;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobData;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobParameterEntry;
import com.mercedesbenz.sechub.pds.tools.GeneratorCommand;
import com.mercedesbenz.sechub.pds.tools.handler.ConsoleHandler;
import com.mercedesbenz.sechub.pds.tools.handler.PrintStreamConsoleHandler;

public class PDSSolutionTestFilesGenerator {

    private TextFileReader reader = new TextFileReader();
    private TextFileWriter writer = new TextFileWriter();

    private File targetFolder;
    private File originConfigFile;

    private ConsoleHandler consoleHandler;
    private ScanType scanType;
    private SecHubConfigurationModel config;

    ConsoleHandler getConsoleHandler() {
        if (consoleHandler == null) {
            consoleHandler = new PrintStreamConsoleHandler();
        }
        return consoleHandler;
    }

    public void setConsoleHandler(ConsoleHandler consoleHandler) {
        this.consoleHandler = consoleHandler;
    }

    public File generate(GeneratorCommand generatorCommand) throws Exception {

        File targetFolder = null;
        String targetFolderPath = generatorCommand.getTargetFolderPath();
        if (targetFolderPath != null && !targetFolderPath.isBlank()) {
            targetFolder = new File(targetFolderPath);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }
        }
        File workingDirectoryFolder = null;
        String workingDirectory = generatorCommand.getWorkingDirectory();

        if (workingDirectory != null && !workingDirectory.isBlank()) {
            workingDirectoryFolder = new File(workingDirectory);
            if (!workingDirectoryFolder.exists()) {
                throw new FileNotFoundException("Working directory " + workingDirectoryFolder.getAbsolutePath() + " does not exist!");
            }
        }
        return generate(generatorCommand.getPathToConfigFile(), generatorCommand.getScanType(), targetFolder, workingDirectoryFolder,
                generatorCommand.isCreateMissingFiles());
    }

    public File generate(String pathToSecHubConfigFile, String wantedScanType, File targetFolderOrNull, File workingDirectory, boolean createMissingFiles)
            throws Exception {
        try {

            ensureScanType(wantedScanType);
            ensureSecHubConfiguration(pathToSecHubConfigFile);

            Path workingDirectoryPath = resolveWorkingDirectory(workingDirectory);

            if (targetFolderOrNull != null) {
                targetFolder = targetFolderOrNull;
            } else {
                targetFolder = Files.createTempDirectory("pds_solution_gen").toFile();
            }

            writeSecHubConfigurationToTempFolder();

            String recucedSecHubConfigJson = writeReducedConfigFile();
            writePDSJobDataFile(recucedSecHubConfigJson);

            ArchiveSupport archiveSupport = new ArchiveSupport();
            archiveSupport.setCreateMissingFiles(createMissingFiles);

            archiveSupport.createArchives(config, workingDirectoryPath, targetFolder.toPath());

            getConsoleHandler().output("Written files to: " + targetFolder.getAbsolutePath());
            return targetFolder;
        } catch (Exception e) {
            getConsoleHandler().error("Generation failed: " + e.getMessage());
            throw e;
        }

    }

    private Path resolveWorkingDirectory(File workingDirectory) {
        if (workingDirectory != null) {
            return workingDirectory.toPath();
        }
        return originConfigFile.getParentFile().toPath();
    }

    private void writeSecHubConfigurationToTempFolder() throws JSONConverterException, IOException {
        writer.writeTextToFile(new File(targetFolder, "original-used-sechub-configfile.json"), JSONConverter.get().toJSON(config, true), false);

    }

    private String writeReducedConfigFile() throws IOException {
        String recucedSecHubConfigJson = SecHubConfigurationModelReducedCloningSupport.DEFAULT.createReducedScanConfigurationCloneJSON(config, scanType);
        SecHubConfigurationModel reducedConfig = JSONConverter.get().fromJSON(SecHubConfigurationModel.class, recucedSecHubConfigJson);
        recucedSecHubConfigJson = JSONConverter.get().toJSON(reducedConfig, true);

        File reducedSecHubConfigFile = new File(targetFolder, "reducedSecHubJson_for_" + scanType.getId() + ".json");
        writer.writeTextToFile(reducedSecHubConfigFile, recucedSecHubConfigJson, true);
        return recucedSecHubConfigJson;
    }

    private void writePDSJobDataFile(String reducedSecHubConfigJson) throws IOException {
        PDSJobData data = new PDSJobData();
        PDSJobParameterEntry entry = new PDSJobParameterEntry();
        entry.key = PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_CONFIGURATION;
        entry.value = reducedSecHubConfigJson;
        data.parameters.add(entry);

        String pdsJobDataJson = JSONConverter.get().toJSON(data, true);
        File pdsJobDataFile = new File(targetFolder, "pdsJobData.json");
        writer.writeTextToFile(pdsJobDataFile, pdsJobDataJson, true);
    }

    private void ensureScanType(String wantedScanType) {
        scanType = findScanTypeByArgument(wantedScanType);
    }

    private void ensureSecHubConfiguration(String pathToSecHubConfigFile) throws IOException {
        originConfigFile = new File(pathToSecHubConfigFile);
        if (!originConfigFile.exists()) {
            throw new FileNotFoundException("Sechub configuration file not found:" + originConfigFile.getAbsolutePath());
        }
        String json = reader.readTextFromFile(originConfigFile);

        config = JSONConverter.get().fromJSON(SecHubConfigurationModel.class, json);
    }

    private ScanType findScanTypeByArgument(String scanType) {
        ScanType result = null;
        for (ScanType foundScanType : ScanType.values()) {
            if (foundScanType.getId().equalsIgnoreCase(scanType)) {
                result = foundScanType;
                break;
            }
        }
        if (isScanTypeNotAccepted(result)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Wanted scan type not accepted:");
            sb.append(scanType);
            sb.append(". Accepted types are: [");
            for (ScanType scanTypeValue : ScanType.values()) {
                if (isScanTypeNotAccepted(scanTypeValue)) {
                    continue;
                }
                sb.append(scanTypeValue.getId());
                sb.append(" ");
            }
            sb.append("]");
            throw new IllegalArgumentException(sb.toString());
        }
        return result;
    }

    private boolean isScanTypeNotAccepted(ScanType type) {
        if (type == null) {
            return true;
        }
        if (type.equals(ScanType.UNKNOWN) || type.equals(ScanType.REPORT)) {
            return true;
        }
        return false;
    }

}
