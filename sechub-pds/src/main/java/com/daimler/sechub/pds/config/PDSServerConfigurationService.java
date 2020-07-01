package com.daimler.sechub.pds.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daimler.sechub.pds.PDSJSONConverterException;
import com.daimler.sechub.pds.PDSProductIdentifierValidator;

import wiremock.com.github.jknack.handlebars.internal.Files;

@Service
public class PDSServerConfigurationService {

    private static final Logger LOG = LoggerFactory.getLogger(PDSServerConfigurationService.class);

    private static final String DEFAULT_PATH = "pds-config.json";

    private static final PDSServerConfiguration FALLBACK_CONFIGURATION = new PDSServerConfiguration();

    @Value("${sechub.pds.config.file:" + DEFAULT_PATH + "}")
    String pathToConfigFile;

    @Autowired
    PDSProductIdentifierValidator productIdValidator;

    private PDSServerConfiguration configuration = FALLBACK_CONFIGURATION;

    @PostConstruct
    protected void postConstruct() {
        Path p = Paths.get(pathToConfigFile);
        File file = p.toFile();
        if (!file.exists()) {
            LOG.error("No config file found at {} - so define empty configuration!", file.getAbsolutePath());
            throw new IllegalStateException("no configuration available, because file missing");
        }
        String json;
        try {
            json = Files.read(file);
            configuration = PDSServerConfiguration.fromJSON(json);
            List<PDSProductSetup> products = configuration.getProducts();
            for (PDSProductSetup setup : products) {
                String productIdErrorMessage = productIdValidator.createValidationErrorMessage(setup.getId());
                if (productIdErrorMessage != null) {
                    throw new IllegalStateException("configuration not valid:" + productIdErrorMessage);
                }
            }

        } catch (IOException | PDSJSONConverterException e) {
            throw new IllegalStateException("no configuration available, because file not valid", e);
        }
    }

    public PDSServerConfiguration getServerConfiguration() {
        return configuration;
    }

}
