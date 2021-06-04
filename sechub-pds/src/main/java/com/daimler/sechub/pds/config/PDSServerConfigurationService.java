// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.PDSJSONConverterException;
import com.daimler.sechub.pds.PDSMustBeDocumented;
import com.daimler.sechub.pds.PDSShutdownService;

@Service
public class PDSServerConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSServerConfigurationService.class);

    private static final String DEFAULT_PATH = "./pds-config.json";

    @PDSMustBeDocumented(value="Define path to PDS configuration file",scope="startup")
    @Value("${sechub.pds.config.file:" + DEFAULT_PATH + "}")
    String pathToConfigFile;

    @Autowired
    PDSShutdownService shutdownService;

    @Autowired
    PDSServerConfigurationValidator serverConfigurationValidator;

    private PDSServerConfiguration configuration;

    private String storageId;

    @PostConstruct
    protected void postConstruct() {
        Path p = Paths.get(pathToConfigFile);
        File file = p.toFile();
        if (file.exists()) {
            try {
                String json = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
                PDSServerConfiguration loadedConfiguration = PDSServerConfiguration.fromJSON(json);
                String message = serverConfigurationValidator.createValidationErrorMessage(loadedConfiguration);
                if (message == null) {
                    configuration = loadedConfiguration;
                } else {
                    LOG.error("configuration file '{}' not valid - reason: {}", file.getAbsolutePath(), message);
                }

            } catch (PDSJSONConverterException | IOException e) {
                LOG.error("no configuration available, because cannot read config file", e);
            }
        } else {
            LOG.error("No config file found at {} !", file.getAbsolutePath());
        }
        if (configuration == null) {
            LOG.error(
                    "PDS configuration failure\n*****************************\nCONFIG ERROR CANNOT START PDS\n*****************************\nNo configuration available (see former logs for reason), so cannot start PDS server - trigger shutdown to ensure application no longer alive");
            shutdownService.shutdownApplication();
        }
        /* define storage id */
        storageId = "pds/" + getServerId();
    }

    public PDSServerConfiguration getServerConfiguration() {
        return configuration;
    }

    public PDSProductSetup getProductSetupOrNull(String productId) {
        for (PDSProductSetup setup : configuration.getProducts()) {
            if (setup.getId().equals(productId)) {
                return setup;
            }
        }
        return null;
    }

    public String getServerId() {
        if (configuration == null) {
            return "undefined-no-configuration";
        }
        return configuration.getServerId();
    }

    /**
     * @return storage ID to use for this PDS server - is always "pds/${serverId}"
     */
    public String getStorageId() {
        return storageId;
    }

}
