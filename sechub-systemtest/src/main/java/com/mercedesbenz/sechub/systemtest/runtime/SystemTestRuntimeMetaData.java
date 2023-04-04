package com.mercedesbenz.sechub.systemtest.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.commons.TextFileReader;
import com.mercedesbenz.sechub.pds.commons.core.PDSJSONConverterException;
import com.mercedesbenz.sechub.pds.commons.core.config.PDSServerConfiguration;
import com.mercedesbenz.sechub.systemtest.config.PDSSolutionDefinition;

class SystemTestRuntimeMetaData {

    private static final Logger LOG = LoggerFactory.getLogger(SystemTestRuntimeMetaData.class);

    private Map<PDSSolutionDefinition, PDSServerConfiguration> pdsSolutionConfigurations = new LinkedHashMap<>();
    private TextFileReader textFileReader;

    public SystemTestRuntimeMetaData() {
        this.textFileReader = new TextFileReader();
    }

    public void register(PDSSolutionDefinition solution, SystemTestRuntimeContext context) {
        LOG.debug("Register solution: {},", solution.getName());

        Path pdsServerConfigFilePath = Paths.get(solution.getPathToPdsServerConfigFile());
        if (!Files.exists(pdsServerConfigFilePath)) {
            throw new WrongConfigurationException(
                    "The calculated PDS server config file does not exist for solution:" + solution.getName() + "!\n" + "Calculated was: "
                            + pdsServerConfigFilePath + "\nYou can set this manually by using 'pathToPdsServerConfigFile' at solution definition level.",
                    context);
        }
        LOG.debug("Read existing PDS server configuration file: {},", pdsServerConfigFilePath);
        String pdsServerConfigurationJson;
        try {
            pdsServerConfigurationJson = textFileReader.loadTextFile(pdsServerConfigFilePath.toFile());
        } catch (IOException e) {
            throw new WrongConfigurationException("Was not able to load PDS server configration file: {}! ", context, e);
        }
        LOG.debug("Fetched PDS server configuration as JSON:\n{},", pdsServerConfigurationJson);

        PDSServerConfiguration configuration;
        try {
            configuration = PDSServerConfiguration.fromJSON(pdsServerConfigurationJson);
        } catch (PDSJSONConverterException e) {
            throw new WrongConfigurationException("The PDS server configration file {} is invalid! ", context, e);
        }

        this.pdsSolutionConfigurations.put(solution, configuration);
    };

    public Collection<PDSServerConfiguration> getPDSServerConfigurations() {
        return pdsSolutionConfigurations.values();
    }

}