// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.developertools.pds;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import com.mercedesbenz.sechub.adapter.AdapterException;
import com.mercedesbenz.sechub.adapter.pds.data.PDSJobData;
import com.mercedesbenz.sechub.adapter.pds.data.PDSJobParameterEntry;
import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.commons.archive.ArchiveConstants;
import com.mercedesbenz.sechub.commons.archive.SecHubFileStructureDataProvider;
import com.mercedesbenz.sechub.commons.core.CommonConstants;
import com.mercedesbenz.sechub.commons.model.JSONConverter;
import com.mercedesbenz.sechub.commons.model.JSONConverterException;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubCodeScanConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModelReducedCloningSupport;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubDataConfigurationObject;
import com.mercedesbenz.sechub.commons.model.SecHubFileSystemConfiguration;
import com.mercedesbenz.sechub.commons.model.SecHubFileSystemContainer;
import com.mercedesbenz.sechub.commons.pds.PDSDefaultParameterKeyConstants;
import com.mercedesbenz.sechub.developertools.DeveloperArchiveSupport;
import com.mercedesbenz.sechub.developertools.OutputHandler;
import com.mercedesbenz.sechub.developertools.SystemOutputHandler;
import com.mercedesbenz.sechub.integrationtest.TextFileWriter;
import com.mercedesbenz.sechub.sharedkernel.configuration.SecHubConfiguration;

public class PDSSolutionTestFilesGenerator {

    private TextFileReader reader = new TextFileReader();
    private TextFileWriter writer = new TextFileWriter();
    private DeveloperArchiveSupport developerArchiveSupport = new DeveloperArchiveSupport();
    private File tmpFolder;
    private File originConfigFile;
    private boolean createPseudoFilesWhenNotExisting = true;

    public static void main(String[] args) throws Exception {
        PDSSolutionTestFilesGenerator geno = new PDSSolutionTestFilesGenerator();
        geno.generate(args);
    }

    private OutputHandler outputHandler;
    private ScanType scanType;
    private SecHubConfigurationModel config;

    OutputHandler getOutputHandler() {
        if (outputHandler == null) {
            outputHandler = new SystemOutputHandler();
        }
        return outputHandler;
    }

    public void setCreatePseudoFilesWhenNotExisting(boolean create) {
        this.createPseudoFilesWhenNotExisting = create;
    }

    public void setOutputHandler(OutputHandler outputHandler) {
        this.outputHandler = outputHandler;
    }

    /* only for command line call - so private */
    private File generate(String[] args) throws Exception {
        if (args.length != 2) {
            getOutputHandler().error("please call with ${pathToSechubConfigFile} ${scanType}");
            throw new IllegalArgumentException("wrong number of parameters");
        }
        return generate(args[0], args[1]);
    }

    /**
     * Generates PSD solution files for development
     *
     * @param pathToSecHubConfigFile
     * @param wantedScanType
     * @return folder where files are generated into
     * @throws Exception
     */
    public File generate(String pathToSecHubConfigFile, String wantedScanType) throws Exception {
        try {

            ensureScanType(wantedScanType);
            ensureSecHubConfiguration(pathToSecHubConfigFile);

            tmpFolder = Files.createTempDirectory("pds_solution_gen").toFile();

            writeSecHubConfigurationToTempFolder();

            String recucedSecHubConfigJson = writeReducedConfigFile();
            writePDSJobDataFile(recucedSecHubConfigJson);

            writeTarAndZipFilesAsConfiguredInSecHubConfigFile();

            getOutputHandler().output("Written files to:" + tmpFolder.getAbsolutePath());
            return tmpFolder;
        } catch (Exception e) {
            getOutputHandler().error("Generation failed:" + e.getMessage());
            throw e;
        }

    }

    private void writeSecHubConfigurationToTempFolder() throws JSONConverterException, IOException {
        writer.save(new File(tmpFolder, "original-used-sechub-configfile.json"), JSONConverter.get().toJSON(config, true), false);

    }

    private void writeTarAndZipFilesAsConfiguredInSecHubConfigFile() throws IOException {

        File extracted = new File(tmpFolder, "extracted");

        File binaryCopy = new File(extracted, "binaries");
        File sourceCopy = new File(extracted, "source");

        copyOriginFilesToExtractedFolder(binaryCopy, sourceCopy);

        developerArchiveSupport.compressToTar(binaryCopy, new File(tmpFolder, CommonConstants.FILENAME_BINARIES_TAR));
        developerArchiveSupport.compressToZip(sourceCopy, new File(tmpFolder, CommonConstants.FILENAME_SOURCECODE_ZIP));

        SecHubFileStructureDataProvider.builder().setModel(config).setScanType(scanType).build();
    }

    private void copyOriginFilesToExtractedFolder(File binaryCopy, File sourceCopy) throws FileNotFoundException, IOException {
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
            File file = new File(originConfigFile.getParentFile(), fileName);
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
                        getOutputHandler().output("WARN: origin folder:" + file.getAbsolutePath() + " did not exist. So created folder at "
                                + targetFile.getAbsolutePath() + " with place holder file.");

                    } else {
                        targetFile.createNewFile();
                        getOutputHandler().output(
                                "WARN: origin file:" + file.getAbsolutePath() + " did not exist. So created pseudo file:" + targetFile.getAbsolutePath());
                    }
                    // we cannot create but have created pseudo-files/empty directories, so stop
                    // here
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

    private String writeReducedConfigFile() throws IOException {
        String recucedSecHubConfigJson = SecHubConfigurationModelReducedCloningSupport.DEFAULT.createReducedScanConfigurationCloneJSON(config, scanType);
        SecHubConfigurationModel reducedConfig = SecHubConfiguration.createFromJSON(recucedSecHubConfigJson);
        recucedSecHubConfigJson = JSONConverter.get().toJSON(reducedConfig, true);

        File reducedSecHubConfigFile = new File(tmpFolder, "reducedSecHubJson_for_" + scanType.getId() + ".json");
        writer.save(reducedSecHubConfigFile, recucedSecHubConfigJson, true);
        return recucedSecHubConfigJson;
    }

    private void writePDSJobDataFile(String recucedSecHubConfigJson) throws AdapterException, IOException {
        PDSJobData data = new PDSJobData();
        PDSJobParameterEntry entry = new PDSJobParameterEntry();
        entry.key = PDSDefaultParameterKeyConstants.PARAM_KEY_PDS_SCAN_CONFIGURATION;
        entry.value = recucedSecHubConfigJson;
        data.parameters.add(entry);

        String pdsJobDataJson = JSONConverter.get().toJSON(data, true);
        File pdsJobDataFile = new File(tmpFolder, "pdsJobData.json");
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
        config = SecHubConfiguration.createFromJSON(json);
    }

    private ScanType findScanTypeByArgument(String scanType) {
        ScanType result = null;
        for (ScanType foundScanType : ScanType.values()) {
            if (foundScanType.getId().equalsIgnoreCase(scanType)) {
                result = foundScanType;
                break;
            }
        }
        if (result == null || result.equals(ScanType.UNKNOWN)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Wanted scan type not accepted:");
            sb.append(scanType);
            sb.append(". Accepted types are: [");
            for (ScanType acceptedScanType : ScanType.values()) {
                if (acceptedScanType.equals(ScanType.UNKNOWN)) {
                    continue;
                }
                sb.append(acceptedScanType.getId());
                sb.append(" ");
            }
            sb.append("]");
            throw new IllegalArgumentException(sb.toString());
        }
        return result;
    }

}
