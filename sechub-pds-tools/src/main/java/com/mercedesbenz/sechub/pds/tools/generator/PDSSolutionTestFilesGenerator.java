// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.tools.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.TextFileWriter;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelReducedCloningSupport;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobData;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobParameterEntry;
import com.mercedesbenz.sechub.pds.tools.handler.ConsoleHandler;
import com.mercedesbenz.sechub.pds.tools.handler.ConsoleOutputHandler;
import com.mercedesbenz.sechub.pds.tools.handler.PrintStreamConsoleHandler;
import com.mercedesbenz.sechub.systemtest.util.SystemTestUploadFilesSupport;

public class PDSSolutionTestFilesGenerator {

    private TextFileReader reader = new TextFileReader();
    private TextFileWriter writer = new TextFileWriter();

    private File targetFolder;
    private File originConfigFile;

    public static void main(String[] args) throws Exception {
        PDSSolutionTestFilesGenerator geno = new PDSSolutionTestFilesGenerator();
        geno.generate(args);
    }

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

    /* only for command line call - so private */
    private File generate(String[] args) throws Exception {
        if (args.length != 2) {
            getConsoleHandler().error("please call with ${pathToSechubConfigFile} ${scanType}");
            throw new IllegalArgumentException("wrong number of parameters");
        }
        return generate(args[0], args[1], null);
    }

    /**
     * Generates PSD solution files for development
     *
     * @param pathToSecHubConfigFile
     * @param wantedScanType
     * @return folder where files are generated into
     * @throws Exception
     */
    public File generate(String pathToSecHubConfigFile, String wantedScanType, File targetFolderOrNull) throws Exception {
        try {

            ensureScanType(wantedScanType);
            ensureSecHubConfiguration(pathToSecHubConfigFile);

            if (targetFolderOrNull != null) {
                targetFolder = targetFolderOrNull;
            } else {
                targetFolder = Files.createTempDirectory("pds_solution_gen").toFile();
            }

            writeSecHubConfigurationToTempFolder();

            String recucedSecHubConfigJson = writeReducedConfigFile();
            writePDSJobDataFile(recucedSecHubConfigJson);

            SystemTestUploadFilesSupport uploadSupport = new SystemTestUploadFilesSupport(originConfigFile.getParentFile(), targetFolder);
            uploadSupport.setOutputHandler(new ConsoleOutputHandler(getConsoleHandler()));

            uploadSupport.createUploadFilesAsConfiguredInSecHubConfigFile(config, scanType);

            getConsoleHandler().output("Written files to:" + targetFolder.getAbsolutePath());
            return targetFolder;
        } catch (Exception e) {
            getConsoleHandler().error("Generation failed:" + e.getMessage());
            throw e;
        }

    }

    private void writeSecHubConfigurationToTempFolder() throws JSONConverterException, IOException {
        writer.save(new File(targetFolder, "original-used-sechub-configfile.json"), JSONConverter.get().toJSON(config, true), false);

    }

    private String writeReducedConfigFile() throws IOException {
        String recucedSecHubConfigJson = SecHubConfigurationModelReducedCloningSupport.DEFAULT.createReducedScanConfigurationCloneJSON(config, scanType);
        SecHubConfigurationModel reducedConfig = JSONConverter.get().fromJSON(SecHubConfigurationModel.class, recucedSecHubConfigJson);
        recucedSecHubConfigJson = JSONConverter.get().toJSON(reducedConfig, true);

        File reducedSecHubConfigFile = new File(targetFolder, "reducedSecHubJson_for_" + scanType.getId() + ".json");
        writer.save(reducedSecHubConfigFile, recucedSecHubConfigJson, true);
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
        writer.save(pdsJobDataFile, pdsJobDataJson, true);
    }

    private void ensureScanType(String wantedScanType) {
        scanType = findScanTypeByArgument(wantedScanType);
    }

    private void ensureSecHubConfiguration(String pathToSecHubConfigFile) throws IOException {
        originConfigFile = new File(pathToSecHubConfigFile);
        if (!originConfigFile.exists()) {
            throw new FileNotFoundException("Sechub configuration file not found:" + originConfigFile.getAbsolutePath());
        }
        String json = reader.loadTextFile(originConfigFile);

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
